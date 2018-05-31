package cz.fi.muni.pv168.bank;


import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Katarina Matusova
 */
public interface PersonManager {
    
    /** Stores new person into database. Id for the new person is automatically
     * generated and stored into id attribute.
     *
     * @param person person to be created.
     */
    void createPerson(Person person);
    
    /** Updates person in database.
     *
     * @param person updated person to be stored into database.
     */
    void updatePerson(Person person);
    
    /** Deletes person from database.
     *
     * @param person person to be deleted from database.
     */
    void deletePerson(Person person);
    
    /** Returns person with given id.
     *
     * @param id primary key of requested person.
     * @return person with given id or null if such person does not exist.
     */
    Person getPerson(Long id);
    
    /**
     * Returns list of all people in the database.
     * @return list of all people in database.
     */
    List<Person> findAllPeople();
    
}
