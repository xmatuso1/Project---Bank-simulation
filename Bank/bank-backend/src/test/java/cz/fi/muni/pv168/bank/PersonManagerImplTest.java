package cz.fi.muni.pv168.bank;

import cz.fi.muni.pv168.common.IllegalEntityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
import javax.sql.DataSource;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.LocalDateTime;
import static java.time.Month.FEBRUARY;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.apache.derby.jdbc.EmbeddedDataSource;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;




/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Katarina Matusova
 */
public class PersonManagerImplTest {
    
    private PersonManagerImpl manager;
    private DataSource dataSource;
    
    private final static ZonedDateTime NOW
            = LocalDateTime.of(2018, FEBRUARY, 28, 14, 00).atZone(ZoneId.of("UTC"));
    
    
    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName("memory:personmgr-test");
        ds.setCreateDatabase("create");
        return ds;
    }
    
    @Before
    public void setUp() throws SQLException {
        dataSource = prepareDataSource();
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("CREATE TABLE PERSON ( "
                    + "ID BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                    + "NAME VARCHAR(255) NOT NULL, "
                    + "BORN DATE NOT NULL )").executeUpdate();
        }           //creating table, to not to change functionality of original table
        manager = new PersonManagerImpl(Clock.fixed(NOW.toInstant(), NOW.getZone()));
        manager.setDataSource(dataSource);
    }

    @After
    public void tearDown() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("DROP TABLE PERSON").executeUpdate();
        }
    }
    
    private PersonBuilder sampleMalePersonBuilder() {
        return new PersonBuilder()
                .id(null)
                .born(LocalDate.of(1985, Month.MARCH, 10))
                .name("John Doe");
    }

    private PersonBuilder sampleFemalePersonBuilder() {
        return new PersonBuilder()
                .born(LocalDate.of(1997, Month.JANUARY, 19))
                .name("Jane Doe");
    }
   
    
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    

    @Test
    public void createPerson() {
        Person person = sampleMalePersonBuilder().build();
        manager.createPerson(person);

        Long personId = person.getId();
        assertNotNull(personId);

        assertThat(manager.getPerson(personId))
                .isNotSameAs(person)
                .isEqualToComparingFieldByField(person);
    }
    
    
    @Test
    public void getPerson() {
        assertNull(manager.getPerson(1L));

        Person person = sampleMalePersonBuilder().build();
        manager.createPerson(person);
        Long personId = person.getId();

        Person result = manager.getPerson(personId);
        assertEquals(person, result);
        assertDeepEquals(person, result);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createNullPerson() {
        manager.createPerson(null);
    }

    @Test
    public void createPersonWithAssignedId() {
        Person person = sampleMalePersonBuilder().id(1L).build();
        exception.expect(IllegalArgumentException.class);
        manager.createPerson(person);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createPersonWithEmptyName() {
        Person person = sampleMalePersonBuilder().name("").build();
        manager.createPerson(person);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createPersonWithNullName() {
        Person person = sampleMalePersonBuilder().name(null).build();
        manager.createPerson(person);
    }
    
    /*@Test
    public void createBodyWithBornTomorrow() {
        LocalDate tomorrow = NOW.toLocalDate().plusDays(1);
        Person person = sampleMalePersonBuilder()
                .born(tomorrow)
                .build();
        
        assertThatThrownBy(() -> manager.createPerson(person))
                .isInstanceOf(IllegalArgumentException.class);
    }*/
    
     @Test
    public void findAllPeople() {

        assertThat(manager.findAllPeople()).isEmpty();

        Person p1 = sampleMalePersonBuilder().build();
        Person p2 = sampleFemalePersonBuilder().build();

        manager.createPerson(p1);
        manager.createPerson(p2);

        assertThat(manager.findAllPeople())
                .usingFieldByFieldElementComparator()
                .containsOnly(p1,p2);
    }
    
   
    @Test
    public void updatePersonName() {
        Person personForUpdate =  sampleMalePersonBuilder().build();
        Person another = sampleFemalePersonBuilder().build();
        manager.createPerson(personForUpdate);
        manager.createPerson(another);

        personForUpdate.setName("John Smith");
        manager.updatePerson(personForUpdate);
        
        assertThat(manager.getPerson(personForUpdate.getId()))
                .isEqualToComparingFieldByField(personForUpdate);
        assertThat(manager.getPerson(another.getId()))
                .isEqualToComparingFieldByField(another);
    }

    
    @Test(expected = IllegalArgumentException.class)
    public void updateNullPerson() {
        manager.updatePerson(null);
    }
   
    
    @Test
    public void updatePersonSetIdNegative() {
        Person person = sampleMalePersonBuilder().build();
        manager.createPerson(person);

        Long personId = person.getId();
        person = manager.getPerson(personId);

        person.setId(-1L);

        exception.expect(IllegalArgumentException.class);
        manager.updatePerson(person);
    }
    
    @Test
    public void updatePersonSetNameEmpty() {
        Person person = sampleMalePersonBuilder().build();
        manager.createPerson(person);

        Long personId = person.getId();
        person = manager.getPerson(personId);

        person.setName("");

        exception.expect(IllegalArgumentException.class);
        manager.updatePerson(person);
    }
    
    @Test
    public void updatePersonSetNameNull() {
        Person person = sampleMalePersonBuilder().build();
        manager.createPerson(person);

        Long personId = person.getId();
        person = manager.getPerson(personId);

        person.setName(null);

        exception.expect(IllegalArgumentException.class);
        manager.updatePerson(person);
    }
    
    @Test
    public void updatePersonSetBornNull() {
        Person person = sampleMalePersonBuilder().build();
        manager.createPerson(person);

        Long personId = person.getId();
        person = manager.getPerson(personId);

        person.setBorn(null);

        exception.expect(IllegalArgumentException.class);
        manager.updatePerson(person);
    }
   
   
    @Test
    public void updatePersonWithNonExistingId() {
        Person person = sampleMalePersonBuilder().id(1L).build();
        exception.expect(IllegalArgumentException.class);
        manager.updatePerson(person);
    }
    
    @Test
    public void deletePerson() {
        Person p1 = sampleMalePersonBuilder().build();
        Person p2 = sampleFemalePersonBuilder().build();
        manager.createPerson(p1);
        manager.createPerson(p2);
        
        assertThat(manager.getPerson(p1.getId())).isNotNull();
        assertThat(manager.getPerson(p2.getId())).isNotNull();

        manager.deletePerson(p1);

        assertThat(manager.getPerson(p1.getId())).isNull();
        assertThat(manager.getPerson(p2.getId())).isNotNull();


    }
    
    @Test(expected = IllegalArgumentException.class)
    public void deleteNullPerson() {
        manager.deletePerson(null);
    }
    
    @Test
    public void deletePersonWithNullId() {
        Person person = sampleMalePersonBuilder().id(null).build();
        exception.expect(IllegalEntityException.class);
        manager.deletePerson(person);
    }
    
    @Test
    public void deletePersonWithNonexistentId() {
        Person person = sampleMalePersonBuilder().id(1L).build();
        exception.expect(IllegalArgumentException.class);
        manager.deletePerson(person);
    }
   
      
    private void assertDeepEquals(Person expected, Person actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getBorn(), actual.getBorn());
    }
    
}

