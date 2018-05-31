package cz.fi.muni.pv168.bank;

import java.util.List;
import javax.sql.DataSource;

/**
 * Interface for BankManagerImpl 
 * @author Katarina Matusova, Aneta Moravcikova
 */
public interface BankManager {
    
    /**
     * Method will open new account that will belong to a certain person
     * @param account account to open
     * @param person owner of the account
     */
    void openNewAccount(Account account ,Person person);
    
    /**
     * Method will delete an account of a certain owner
     * @param account account to delete
     * @param person owner of account to delete
     */
    void deleteAccount(Account account ,Person person);
    
    /**
     * Method will transfer a certain amount of money from one account to another
     * @param sum money to transfer
     * @param from paying account 
     * @param to recieving account
     */
    void processPayment(double sum, Account from ,Account to);
    
    /**
     * Method will return a list of all accounts owned by a certain person
     * @param person owner
     * @return
     */
    List<Account> findAllAccountsByPerson(Person person);
    
    /**
     * Method will return an owner of a certain account
     * @param account account
     * @return
     */
    Person findPersonWithAccount(Account account);
    
    /**
     * Method sets data source.
     * @param dataSource dataSource
     */
    public void setDataSource(DataSource dataSource);
}