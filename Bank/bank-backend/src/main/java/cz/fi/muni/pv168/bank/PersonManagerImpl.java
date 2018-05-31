package cz.fi.muni.pv168.bank;


import cz.fi.muni.pv168.common.IllegalEntityException;
import cz.fi.muni.pv168.common.ServiceFailureException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *  This class implements PersonManager service.
 * 
 * @author Katarina Matusova
 */

public class PersonManagerImpl implements PersonManager {
  

    private static final Logger logger = Logger.getLogger(PersonManagerImpl.class.getName());

    private final Clock clock;
    private DataSource dataSource;
    
    public PersonManagerImpl(Clock clock) {
        this.clock = clock;
    }

    public PersonManagerImpl(DataSource dataSource) {
        this.clock = null;
        this.dataSource = dataSource;
    }

    
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Person getPerson(Long id) throws ServiceFailureException {
        checkDataSource();
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }   
        try (Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement(
                        "SELECT ID,NAME,BORN FROM PERSON WHERE ID = ?")) {
  
            st.setLong(1, id);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                Person person = new Person();
                person.setName(rs.getString("NAME"));
                person.setBorn(rs.getDate("BORN").toLocalDate());
                person.setId(rs.getLong("ID")); 
                
                if (rs.next()) {
                    throw new ServiceFailureException(
                            "Internal error: More people with the same id found ");
                }
                return person;
            } else {
                return null;
            }

        } catch (SQLException ex) {
            String msg = "Error when getting person with id = " + id + " from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }
    }
    
    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }


    @Override
    public void createPerson(Person person) throws ServiceFailureException {
        checkDataSource();
        validate(person);
        if (person.getId() != null) {
            throw new IllegalArgumentException("person id is already set");     
        }
        Long id;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement("INSERT INTO Person (name,born) VALUES (?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            
            st.setString(1, person.getName());
            st.setDate(2, Date.valueOf(person.getBorn()));
            int addedRows = st.executeUpdate();
            if (addedRows != 1) {
                throw new ServiceFailureException("Internal Error: More rows ("
                        + addedRows + ") inserted when trying to insert person " + person);
            }

            ResultSet keyRS = st.getGeneratedKeys();
            id = getKey(keyRS);
            person.setId(id);
            
        } catch (SQLException ex) {
            String msg = "Error when inserting person into db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } catch (Exception ex) {
            throw new ServiceFailureException("Error when inserting person " + person, ex);
        }
    }

    
    @Override
    public void updatePerson(Person person) {
        checkDataSource();
        validate(person);
        if (person.getId() == null) {
            throw new IllegalEntityException("person id is null.");
        }
        
        if (person.getId() < 0) {
            throw new IllegalArgumentException("person id is negative.");
        }
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement("UPDATE Person SET name = ?, born = ? WHERE id = ?")) {
            st.setString(1, person.getName());
            st.setDate(2, Date.valueOf(person.getBorn()));
            st.setLong(3, person.getId());

            int count = st.executeUpdate();
            if (count == 0) {
                throw new IllegalArgumentException("Person " + person + " was not found in database!");
            } else if (count != 1) {
                throw new IllegalArgumentException("Invalid updated rows count detected (one row should be updated): " + count);
            }
            
        } catch (SQLException ex) {
            String msg = "Error when updating person in the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }
    }

    @Override
    public void deletePerson(Person person) {
    checkDataSource();
        if (person == null) {
            throw new IllegalArgumentException("person is null");
        }        
        if (person.getId() == null) {
            throw new IllegalEntityException("person id is null");
        }        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement("DELETE FROM Person WHERE id = ?")) {
            st.setLong(1, person.getId());

            int count = st.executeUpdate();
            if (count == 0) {
                throw new IllegalArgumentException("Person " + person + " was not found in database!");
            } else if (count != 1) {
                throw new IllegalArgumentException("Invalid deleted rows count detected (one row should be updated): " + person);
            }
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when deleting person from the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }
    }
   

    @Override
    public List<Person> findAllPeople() {
    try (
                Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "SELECT ID,NAME,BORN FROM PERSON")) {

            ResultSet rs = st.executeQuery();

            List<Person> result = new ArrayList<>();
            while (rs.next()) {
                Person person = new Person();
                person.setName(rs.getString("NAME"));
                person.setBorn(rs.getDate("BORN").toLocalDate());
                person.setId(rs.getLong("ID"));
                result.add(person);
            }
            return result;

        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when retrieving all people", ex);
        }
    }
   
    
    private void validate(Person person) throws IllegalArgumentException {
        if (person == null) {
            throw new IllegalArgumentException("person is null.");
        }

        if (person.getName() == null) {
            throw new IllegalArgumentException("name is null.");
        }
        if (person.getName().isEmpty()) {
            throw new IllegalArgumentException("person with empty name.");
        }

        if (person.getBorn()==null) {
            throw new IllegalArgumentException("born is null.");
        }
       /** LocalDate today = LocalDate.now(clock);
        if (person.getBorn().isAfter(today)) {
            throw new IllegalArgumentException("born is in future");
        }*/
       
    }
    
    
    private static Long getKey(ResultSet key) throws SQLException {
        if (key.next()) {
            if (key.getMetaData().getColumnCount() != 1) {
                throw new IllegalArgumentException("Given ResultSet contains more columns");
            }
            Long result = key.getLong(1);
            if (key.next()) {
                throw new IllegalArgumentException("Given ResultSet contains more rows");
            }
            return result;
        } else {
            throw new IllegalArgumentException("Given ResultSet contain no rows");
        }
    }
    
}