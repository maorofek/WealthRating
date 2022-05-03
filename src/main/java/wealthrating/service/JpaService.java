package wealthrating.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wealthrating.boundary.PersonBoundary;
import wealthrating.dal.Person;
import wealthrating.dal.PersonDao;
import wealthrating.exeption.EntityNotFoundException;
import wealthrating.util.Converter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class JpaService {
    private final PersonDao personDao;
    private final Converter converter;

    @Autowired
    public JpaService(PersonDao personDao, Converter converter) {
        this.personDao = personDao;
        this.converter = converter;
    }

    @Transactional
    public void savePerson(PersonBoundary personBoundary) {
        personDao.save(converter.boundaryToEntity(personBoundary));
    }

    @Transactional(readOnly = true)
    public List<PersonBoundary> getAllRichPeople() {
        return StreamSupport.stream(personDao.findAll().spliterator(), false) // Iterable -> Stream
                .map(converter::entityToBoundary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PersonBoundary getRichById(Integer id) {
        Optional<Person> optional = personDao.findById(id);
        return converter.entityToBoundary(optional.orElseThrow(() -> new EntityNotFoundException("No such rich person")));
    }
}