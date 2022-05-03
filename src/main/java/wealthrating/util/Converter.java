package wealthrating.util;

import org.springframework.stereotype.Component;
import wealthrating.boundary.FinancialInfo;
import wealthrating.boundary.PersonBoundary;
import wealthrating.boundary.PersonalInfo;
import wealthrating.dal.Person;

@Component // Singleton of Spring
public class Converter {

    public PersonBoundary entityToBoundary(Person person) {
        return PersonBoundary
                .builder()
                .id(person.getId())
                .personalInfo(new PersonalInfo(person.getFirstName(), person.getLastName(), person.getCity()))
                .financialInfo(new FinancialInfo(person.getCash(), person.getNumberOfAssets()))
                .build();
    }

    public Person boundaryToEntity(PersonBoundary personBoundary) {
        return Person
                .builder()
                .id(personBoundary.getId())
                .firstName(personBoundary.getPersonalInfo().getFirstName())
                .lastName(personBoundary.getPersonalInfo().getLastName())
                .city(personBoundary.getPersonalInfo().getCity())
                .cash(personBoundary.getFinancialInfo().getCash())
                .numberOfAssets(personBoundary.getFinancialInfo().getNumberOfAssets())
                .build();
    }
}