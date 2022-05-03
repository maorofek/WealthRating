package wealthrating;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import wealthrating.boundary.FinancialInfo;
import wealthrating.boundary.PersonBoundary;
import wealthrating.boundary.PersonalInfo;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static wealthrating.controller.Controller.BASE_URL;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WealthRatingApplicationTests {

    private int port;
    private final int FIRST_ID_TEST = 1;
    private int currentId = FIRST_ID_TEST;
    private final RestTemplate restTemplate = new RestTemplate();
    private String baseUrl;
    private String secretCode;

    @LocalServerPort
    public void setPort(int port) {
        this.port = port;
    }

    @Value("${secretCode}")
    public void setSecretCode(String secretCode) {
        this.secretCode = secretCode;
    }

    @PostConstruct
    public void init() {
        baseUrl = "http://localhost:" + port + "/" + BASE_URL;
    }

    @Test()
    @Order(1)
    void handleNotRichPersonTest() {
        PersonBoundary personBoundary = createPersonAndHandle(false);

        //means server returns http error 404
        HttpClientErrorException thrown = catchThrowableOfType(
                () -> restTemplate.getForObject(baseUrl + "getbyid?id={id}", PersonBoundary.class,
                        personBoundary.getId()), HttpClientErrorException.class
        );
        assertThat(thrown).isNotNull();
        assertThat(thrown.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(2)
    void handleRichPersonTest() {
        PersonBoundary personBoundary = createPersonAndHandle(true);

        PersonBoundary DBResult = restTemplate.getForObject(baseUrl + "getbyid?id={id}", PersonBoundary.class, personBoundary.getId());
        assertThat(DBResult).isNotNull();
        assertThat(personBoundary).usingRecursiveComparison().isEqualTo(DBResult);
    }

    private PersonBoundary createPersonAndHandle(boolean isRich) {
        PersonBoundary personBoundary = PersonBoundary
                .builder()
                .id(currentId++)
                .personalInfo(new PersonalInfo("King", "Jorge", "New-York"))
                .financialInfo(new FinancialInfo((isRich ? 1L : -1L) * 10000L, 50))
                .build();

        Map<String, Object> map = restTemplate.postForObject(baseUrl + "handle?secretCode={secretCode}",
                personBoundary, Map.class, secretCode);
        assertThat(map).isNotNull();
        assertThat(map).containsEntry("id", personBoundary.getId());
        assertThat(map).containsEntry("city", personBoundary.getPersonalInfo().getCity());
        assertThat(map).containsKey("isRich");
        assertThat(map.get("isRich")).isEqualTo(isRich);
        assertThat(map).containsKey("fortune");
        map.put("fortune", Long.parseLong(map.get("fortune").toString())); //handle when JSON value is only in Integer range
        assertThat(map.get("fortune")).isEqualTo(personBoundary.getFinancialInfo().getCash());

        return personBoundary;
    }

    @Test
    @Order(3)
    void getByIdTest() {
        PersonBoundary personBoundary = createPersonAndHandle(true);
        PersonBoundary DBResult = restTemplate.getForObject(baseUrl + "getbyid?id={id}", PersonBoundary.class, personBoundary.getId());
        assertThat(DBResult).isNotNull();
        assertThat(personBoundary).usingRecursiveComparison().isEqualTo(DBResult);
    }

    @Test
    @Order(4)
    void getAllRichPeopleTest() {
        List<PersonBoundary> boundaries = IntStream.range(0, 5)
                .mapToObj(x -> createPersonAndHandle(true))
                .collect(Collectors.toList());
        PersonBoundary[] DBResult = restTemplate.getForObject(baseUrl + "getall", PersonBoundary[].class);
        assertThat(DBResult).isNotNull();
        assertThat(DBResult)
                .hasSizeGreaterThanOrEqualTo(5)
                .usingRecursiveFieldByFieldElementComparator()
                .containsAll(boundaries);
    }
}