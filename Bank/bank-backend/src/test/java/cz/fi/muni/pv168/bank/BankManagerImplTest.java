/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.fi.muni.pv168.bank;

import cz.fi.muni.pv168.common.ServiceFailureException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import static java.time.Month.FEBRUARY;
import static java.time.Month.MARCH;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import javax.sql.DataSource;
import static junit.framework.Assert.assertEquals;
import org.apache.derby.jdbc.EmbeddedDataSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test class for BankManagerImpl
 * @author Katarina Matusova
 */
public class BankManagerImplTest {
    
    private BankManagerImpl manager;
    private PersonManagerImpl personManager;
    private AccountManagerImpl accountManager;
    private DataSource ds;
    
    private final static ZonedDateTime NOW
            = LocalDateTime.of(2018, Month.FEBRUARY, 28, 14, 00).atZone(ZoneId.of("UTC"));
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();    
    
    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName("memory:bankmgr-test");
        ds.setCreateDatabase("create");
        return ds;
    }
    
     @Before
    public void setUp() throws SQLException {
        ds = prepareDataSource();
        try (Connection connection = ds.getConnection()) {
            connection.prepareStatement("CREATE TABLE PERSON ( "
                    + "ID BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                    + "NAME VARCHAR(255) NOT NULL, "
                    + "BORN DATE NOT NULL )").executeUpdate();
            connection.prepareStatement("CREATE TABLE ACCOUNT ( "
                    + "NUMBER BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                    + "NOTE VARCHAR(255), "
                    + "BALANCE DECIMAL (20,2) ,"
                    + "PERSONID BIGINT REFERENCES PERSON (ID))").executeUpdate();
        }
        manager = new BankManagerImpl();
        manager.setDataSource(ds);
        personManager = new PersonManagerImpl(Clock.fixed(NOW.toInstant(), NOW.getZone()));
        personManager.setDataSource(ds);
        accountManager = new AccountManagerImpl();
        accountManager.setDataSource(ds);
        prepareTestData();
    }

    
    @After
    public void tearDown() throws SQLException {
        try (Connection connection = ds.getConnection()) {
            connection.prepareStatement("DROP TABLE ACCOUNT").executeUpdate();
            connection.prepareStatement("DROP TABLE PERSON").executeUpdate();
        }
    }
    
    private Person p1, p2, p3, personWithNullId, personNotInDB;
    private Account a1, a2, a3, a4, accountWithNullNumber, accountNotInDB;
    
    private void prepareTestData() {
       
        p1 = new PersonBuilder().name("John Doe").born(LocalDate.of(1985,MARCH, 20)).build();
        p2 = new PersonBuilder().name("Jane Doe").born(LocalDate.of(2000, MARCH, 10)).build();
        p3 = new PersonBuilder().name("Brad Smith").born(LocalDate.of(2017, FEBRUARY, 1)).build();
        
        a1 = new AccountBuilder().balance(500).build();
        a2 = new AccountBuilder().balance(0).build();   
        a3 = new AccountBuilder().balance(20.50).build();
        a4 = new AccountBuilder().balance(7000.0).build();
        
        personManager.createPerson(p1);   
        personManager.createPerson(p2);   
        personManager.createPerson(p3);
        
        accountManager.createAccount(a1);
        accountManager.createAccount(a2);
        accountManager.createAccount(a3);
        accountManager.createAccount(a4);

        accountWithNullNumber = new AccountBuilder().number(null).build();
        accountNotInDB = new AccountBuilder().number(a3.getNumber() + 100).build();
        assertThat(accountManager.getAccount(accountNotInDB.getNumber())).isNull();

        personWithNullId = new PersonBuilder().name("Person with null id").id(null).build();
        personNotInDB = new PersonBuilder().name("Person not in DB").id(p3.getId() + 100).build();
        assertThat(personManager.getPerson(personNotInDB.getId())).isNull();
    }
    
    @Test
    public void openNewAccount() {

        assertThat(manager.findPersonWithAccount(a1)).isNull();
        assertThat(manager.findPersonWithAccount(a2)).isNull();
        assertThat(manager.findPersonWithAccount(a3)).isNull();
        assertThat(manager.findPersonWithAccount(a4)).isNull();

        manager.openNewAccount(a1, p3);
        manager.openNewAccount(a4, p1);
        manager.openNewAccount(a3, p3);

        assertThat(manager.findAllAccountsByPerson(p1))
                .usingFieldByFieldElementComparator()
                .containsOnly(a4);
        assertThat(manager.findAllAccountsByPerson(p2))
                .isEmpty();
        assertThat(manager.findAllAccountsByPerson(p3))
                .usingFieldByFieldElementComparator()
                .containsOnly(a1,a3);

        assertThat(manager.findPersonWithAccount(a1))
                .isEqualToComparingFieldByField(p3);
        assertThat(manager.findPersonWithAccount(a2))
                .isNull();
        assertThat(manager.findPersonWithAccount(a3))
                .isEqualToComparingFieldByField(p3);
        assertThat(manager.findPersonWithAccount(a4))
                .isEqualToComparingFieldByField(p1);
       
    }
   @Test
    public void openNewAccountMultipleTime() {

        manager.openNewAccount(a1, p3);
        manager.openNewAccount(a4, p1);
        manager.openNewAccount(a3, p3);

        assertThatThrownBy(() -> manager.openNewAccount(a1, p3))
                .isInstanceOf(IllegalArgumentException.class);

       
        assertThat(manager.findAllAccountsByPerson(p1))
                .usingFieldByFieldElementComparator()
                .containsOnly(a4);
        assertThat(manager.findAllAccountsByPerson(p2))
                .isEmpty();
        assertThat(manager.findAllAccountsByPerson(p3))
                .usingFieldByFieldElementComparator()
                .containsOnly(a1,a3);
    }

    @Test
    public void openAccountToMultiplePeople() {

        manager.openNewAccount(a1, p3);
        manager.openNewAccount(a4, p1);
        manager.openNewAccount(a3, p3);

        assertThatThrownBy(() -> manager.openNewAccount(a1, p2))
                .isInstanceOf(IllegalArgumentException.class);

       
        assertThat(manager.findAllAccountsByPerson(p1))
                .usingFieldByFieldElementComparator()
                .containsOnly(a4);
        assertThat(manager.findAllAccountsByPerson(p2))
                .isEmpty();
        assertThat(manager.findAllAccountsByPerson(p3))
                .usingFieldByFieldElementComparator()
                .containsOnly(a1,a3);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void openNewNullAccount() {
        manager.openNewAccount(null, p2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void openNewAccountWithNullNumber() {
        manager.openNewAccount(accountWithNullNumber, p2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void openNewAccountNotInDB() {
        manager.openNewAccount(accountNotInDB, p2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void openNewAccountToNullPerson(){
        manager.openNewAccount(a2, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void openNewAccountWithNullId() {
        manager.openNewAccount(a2, personWithNullId);
    }

    @Test(expected = ServiceFailureException.class)
    public void openNewAccountToPersonNotInDB() {
        manager.openNewAccount(a2, personNotInDB);
    }
    
    
    @Test
    public void deleteAccount() {

        manager.openNewAccount(a1,p3);
        manager.openNewAccount(a3,p3);
        manager.openNewAccount(a4,p1);

        assertThat(manager.findPersonWithAccount(a1))
                .isEqualToComparingFieldByField(p3);
        assertThat(manager.findPersonWithAccount(a2))
                .isNull();
        assertThat(manager.findPersonWithAccount(a3))
                .isEqualToComparingFieldByField(p3);
        assertThat(manager.findPersonWithAccount(a4))
                .isEqualToComparingFieldByField(p1);

        manager.deleteAccount(a3,p3);

        assertThat(manager.findAllAccountsByPerson(p3))
                .usingFieldByFieldElementComparator()
                .containsOnly(a1);
        assertThat(manager.findAllAccountsByPerson(p2))
                .isEmpty();
        assertThat(manager.findAllAccountsByPerson(p1))
                .usingFieldByFieldElementComparator()
                .containsOnly(a4);


        assertThat(manager.findPersonWithAccount(a1))
                .isEqualToComparingFieldByField(p3);
        assertThat(manager.findPersonWithAccount(a2))
                .isNull();
        assertThat(manager.findPersonWithAccount(a3))
                .isNull();
        assertThat(manager.findPersonWithAccount(a4))
                .isEqualToComparingFieldByField(p1);
    }
    
     @Test
    public void deleteAccountToWrongPerson() {

        manager.openNewAccount(a1,p3);
        manager.openNewAccount(a2,p3);
        manager.openNewAccount(a3,p1);

        assertThatThrownBy(() -> manager.openNewAccount(a1,p1))
                .isInstanceOf(IllegalArgumentException.class);

        
        assertThat(manager.findAllAccountsByPerson(p1))
                .usingFieldByFieldElementComparator()
                .containsOnly(a3);
        assertThat(manager.findAllAccountsByPerson(p2))
                .isEmpty();
        assertThat(manager.findAllAccountsByPerson(p3))
                .usingFieldByFieldElementComparator()
                .containsOnly(a1,a2);
    }      
    
     @Test(expected = IllegalArgumentException.class)
    public void deleteNullAccount (){
        manager.deleteAccount(null,p2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteAccountWithNullNumber() {
        manager.deleteAccount(accountWithNullNumber,p2);
    }
    
    
    @Test(expected = IllegalArgumentException.class)
    public void deleteAccountNotInDB() {
        manager.deleteAccount(accountNotInDB, p2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteAccountWithNullPerson() {
        manager.deleteAccount(a1, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteAccountToPersonWithNullId(){
        manager.deleteAccount(a2,personWithNullId);
    }

     
    @Test(expected = IllegalArgumentException.class)
    public void deleteAccountToPersonNotInDB(){
        manager.deleteAccount(a2,personNotInDB);
    }
    
    @Test
    public void findAllAccounts() {

        assertThat(manager.findAllAccountsByPerson(p1)).isEmpty();
        assertThat(manager.findAllAccountsByPerson(p2)).isEmpty();
        assertThat(manager.findAllAccountsByPerson(p3)).isEmpty();

        manager.openNewAccount(a1, p3);
        manager.openNewAccount(a2, p3);
        manager.openNewAccount(a3, p2);
        manager.openNewAccount(a4, p3);        

        assertThat(manager.findAllAccountsByPerson(p1))
                .isEmpty();
        assertThat(manager.findAllAccountsByPerson(p2))
                .usingFieldByFieldElementComparator()
                .containsOnly(a3);
        assertThat(manager.findAllAccountsByPerson(p3))
                .usingFieldByFieldElementComparator()
                .containsOnly(a1,a2,a4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAccountsWithNullPerson() {
        manager.findAllAccountsByPerson(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAccountsWithPersonHavingNullId() {
        manager.findAllAccountsByPerson(personWithNullId);
    }
    
    @Test
    public void findPersonWithAccount() {

        assertThat(manager.findPersonWithAccount(a1)).isNull();
        assertThat(manager.findPersonWithAccount(a2)).isNull();
        assertThat(manager.findPersonWithAccount(a3)).isNull();
        assertThat(manager.findPersonWithAccount(a4)).isNull();

        manager.openNewAccount(a1, p3);

        assertThat(manager.findPersonWithAccount(a1))
                .isEqualToComparingFieldByField(p3);
        assertThat(manager.findPersonWithAccount(a2)).isNull();
        assertThat(manager.findPersonWithAccount(a3)).isNull();
        assertThat(manager.findPersonWithAccount(a4)).isNull();
    }
        
    @Test(expected = IllegalArgumentException.class)
    public void findPersonWithNullAccount() {
        manager.findPersonWithAccount(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findPersonWithAccountHavingNullId() {
        manager.findPersonWithAccount(accountWithNullNumber);
    }
    
    @Test
    public void processPayment(){
        
        double balance1 = a1.getBalance();
        double balance2 = a2.getBalance();
        
        manager.openNewAccount(a1, p3);
        manager.openNewAccount(a2, p1);
        
        manager.processPayment(200, a1, a2);
        
        assertEquals(balance1 - 200, manager.findAllAccountsByPerson(p3).get(0).getBalance());
        assertEquals(balance2 + 200, manager.findAllAccountsByPerson(p1).get(0).getBalance());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void processPaymentWithNullFromAccount() {
        manager.processPayment(100.0,null,a1);
    }
    
   @Test(expected = IllegalArgumentException.class)
    public void processPaymentWithNullToAccount() {
        manager.processPayment(100.0 ,a1, null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void processPaymentWithNegativeSum() {
        manager.processPayment(-500,a3,a1);
    }
    

   
    
}
