package wealthrating.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import wealthrating.boundary.PersonBoundary;
import wealthrating.exeption.InvalidInputException;
import wealthrating.exeption.ThirdPartyResponseException;
import wealthrating.service.JpaService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class Controller {
    private final JpaService jpaService;
    private static final String CENTRAL_BANK_BASE_URL = "https://www.some-demo-bank.com/central-bank/";
    private static final String CENTRAL_BANK_EVALUATE = "regional-info/evaluate?city={city}";
    private static final String CENTRAL_BANK_THRESHOLD = "wealth-threshold";
    public static final String BASE_URL = "/wealthrating/";

    private String secretCode;

    @Autowired
    public Controller(JpaService jpaService) {
        this.jpaService = jpaService;
    }

    @RequestMapping(
            method = RequestMethod.POST,
            path = BASE_URL + "handle",
            produces = APPLICATION_JSON_VALUE,
            consumes = APPLICATION_JSON_VALUE
    )
    public Map<String, Object> handleRequest(@RequestBody PersonBoundary personBoundary,
                                             @RequestParam(name = "secretCode", required = false, defaultValue = "") String secretCode) {
        boolean isTestMode = secretCode.equals(this.secretCode);
        validate(personBoundary);
        RestTemplate restTemplate = new RestTemplate();
        long cityAssetValue;
        long thresholdValue;
        if (!isTestMode) {
            Map<String, Object> evaluateResponse = restTemplate.getForObject(CENTRAL_BANK_BASE_URL + CENTRAL_BANK_EVALUATE,
                    Map.class, personBoundary.getPersonalInfo().getCity());

            if (evaluateResponse == null) {
                throw new ThirdPartyResponseException("response from central bank is null");
            }

            cityAssetValue = Long.parseLong(evaluateResponse.get("cityAssetValue").toString());

            Map<String, Object> thresholdResponse = restTemplate.getForObject(CENTRAL_BANK_BASE_URL + CENTRAL_BANK_THRESHOLD, Map.class);

            if (thresholdResponse == null) {
                throw new ThirdPartyResponseException("response from central bank is null");
            }

            thresholdValue = Long.parseLong(thresholdResponse.get("thresholdValue").toString());
        } else {
            //ez to test because richness depends only on cash sign(+/-)
            cityAssetValue = 0;
            thresholdValue = 0;
        }
        long fortune = personBoundary.getFinancialInfo().getCash() + personBoundary.getFinancialInfo().getNumberOfAssets() * cityAssetValue;

        if (fortune > thresholdValue) {
            jpaService.savePerson(personBoundary);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", personBoundary.getId());
        response.put("city", personBoundary.getPersonalInfo().getCity());
        response.put("fortune", fortune);
        response.put("isRich", fortune > thresholdValue);

        return response;
    }

    @RequestMapping(
            method = RequestMethod.GET,
            path = BASE_URL + "getbyid",
            produces = APPLICATION_JSON_VALUE
    )
    public PersonBoundary getRichById(@RequestParam(name = "id") Integer id) {
        return jpaService.getRichById(id);
    }

    @RequestMapping(
            method = RequestMethod.GET,
            path = BASE_URL + "getall",
            produces = APPLICATION_JSON_VALUE
    )
    public List<PersonBoundary> getAllRichPeople() {
        return jpaService.getAllRichPeople();
    }

    private void validate(PersonBoundary personBoundary) {
        if (personBoundary == null) {
            throw new InvalidInputException("person Boundary is null");
        }
        if (personBoundary.getPersonalInfo() == null) {
            throw new InvalidInputException("Personal Info is null");
        }
        if (personBoundary.getFinancialInfo() == null) {
            throw new InvalidInputException("Financial Info is null");
        }
        if (personBoundary.getPersonalInfo().getFirstName() == null || personBoundary.getPersonalInfo().getFirstName().isEmpty()) {
            throw new InvalidInputException("First Name is null");
        }
        if (personBoundary.getPersonalInfo().getLastName() == null || personBoundary.getPersonalInfo().getLastName().isEmpty()) {
            throw new InvalidInputException("Last Name is null");
        }
        if (personBoundary.getPersonalInfo().getCity() == null || personBoundary.getPersonalInfo().getCity().isEmpty()) {
            throw new InvalidInputException("city is null");
        }
        if (personBoundary.getFinancialInfo().getCash() == null) {
            throw new InvalidInputException("cash is null");
        }
        if (personBoundary.getFinancialInfo().getNumberOfAssets() == null) {
            throw new InvalidInputException("Number Of Assets is null");
        }
    }

    @Value("${secretCode}")
    public void setSecretCode(String secretCode) {
        this.secretCode = secretCode;
    }
}