package cz.fi.muni.pv168.bank;



import cz.fi.muni.pv168.common.IllegalEntityException;
import java.sql.Connection;
import org.junit.Rule;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.*;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
/**
 * Test class for AccountManagerImpl.
 * @author Aneta moravcikova, uco 456444
 */
public class AccountManagerImplTest {
    
    private AccountManagerImpl manager;
    private DataSource ds;    
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName("memory:accountmgr-test");
        ds.setCreateDatabase("create");
        return ds;
    }
    
    @Before
    public void setUp() throws SQLException{
        ds = prepareDataSource();        
        try (Connection connection = ds.getConnection()) {
            connection.prepareStatement("CREATE TABLE PERSON ( "
                    + "ID BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                    + "NAME VARCHAR(255) NOT NULL, "
                    + "BORN DATE NOT NULL )").executeUpdate();
            connection.prepareStatement("CREATE TABLE ACCOUNT ( "
                    + "NUMBER BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                    + "NOTE VARCHAR(255), "
                    + "BALANCE DOUBLE ,"
                    + "PERSONID BIGINT REFERENCES PERSON (ID))").executeUpdate();
        }
        manager = new AccountManagerImpl();
        manager.setDataSource(ds);
        
    }
    
    @After
    public void tearDown() throws SQLException {
        try (Connection connection = ds.getConnection()) {
            connection.prepareStatement("DROP TABLE ACCOUNT").executeUpdate();
            connection.prepareStatement("DROP TABLE PERSON").executeUpdate();
        }
    }
    
    private AccountBuilder sampleSmallAccountBuilder(){
        return new AccountBuilder()
                .note("student account")
                .balance(123.8);
    }
     
    private AccountBuilder sampleBigAccountBuilder(){
        return new AccountBuilder()
                .balance(75000.5)
                .note("big account");
    }
    
    @Test 
    public void createAccount(){
        Account account = sampleSmallAccountBuilder().build();
        manager.createAccount(account);
        
        Long accountNumber = account.getNumber();
        assertThat(accountNumber).isNotNull();
        
        assertThat(manager.getAccount(accountNumber)).
                isEqualToComparingFieldByField(account);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createNullAccount() {
        manager.createAccount(null);
    }
    
    @Test                                                                           
    public void createAccountWithExistingNumber() {
        Account account = sampleSmallAccountBuilder().number(1L).build();
        expectedException.expect(IllegalArgumentException.class);
        manager.createAccount(account);
    }
    
    
    @Test
    public void createAccountWithZeroBalance() {
        Account account = sampleSmallAccountBuilder().balance(0.0).build();
        manager.createAccount(account);

        assertThat(manager.getAccount(account.getNumber()))
                .isNotNull()
                .isEqualToComparingFieldByField(account);
    }
    
    
    @Test 
    public void updateAccountBalance(){
        Account accForUpdate = sampleSmallAccountBuilder().build();
        Account another = sampleBigAccountBuilder().build();
        manager.createAccount(accForUpdate);
        manager.createAccount(another);
        
        accForUpdate.setBalance(1.0);
        manager.updateAccount(accForUpdate);
        
        assertThat(manager.getAccount(accForUpdate.getNumber()))
                .isEqualToComparingFieldByField(accForUpdate);
        assertThat(manager.getAccount(another.getNumber()))
                .isEqualToComparingFieldByField(another);
        
    }
    
    @Test
    public void getPerson() {
        assertNull(manager.getAccount(1L));

        Account account = sampleSmallAccountBuilder().build();
        manager.createAccount(account);
        Long accountNumber = account.getNumber();

        Account result = manager.getAccount(accountNumber);
        assertEquals(account, result);
        assertThat(manager.getAccount(result.getNumber()))
                .isEqualToComparingFieldByField(account);
    }
    
    @Test 
    public void updateAccountNote(){
        String newNote = "newNote";
        Account accForUpdate = sampleSmallAccountBuilder().build();
        Account another = sampleBigAccountBuilder().build();
        manager.createAccount(accForUpdate);
        manager.createAccount(another);
        
        accForUpdate.setNote(newNote);
        manager.updateAccount(accForUpdate);
        
        assertThat(manager.getAccount(accForUpdate.getNumber()))
                .isEqualToComparingFieldByField(accForUpdate);
        assertThat(manager.getAccount(another.getNumber()))
                .isEqualToComparingFieldByField(another);
    }
    @Test(expected = IllegalArgumentException.class)
    public void updateNullAccount() {
        manager.updateAccount(null);
    }   

    @Test(expected = IllegalArgumentException.class)
    public void deleteNullAccount() {
        manager.deleteAccount(null);
    }
    
    @Test
    public void deleteAccount() {

        Account a1 = sampleSmallAccountBuilder().build();
        Account a2 = sampleBigAccountBuilder().build();
        manager.createAccount(a1);
        manager.createAccount(a2);

        assertThat(manager.getAccount(a1.getNumber())).isNotNull();
        assertThat(manager.getAccount(a2.getNumber())).isNotNull();

        manager.deleteAccount(a1);

        assertThat(manager.getAccount(a1.getNumber())).isNull();
        assertThat(manager.getAccount(a2.getNumber())).isNotNull();

    }

    @Test
    public void deleteAccountWithNullId() {
        Account account = sampleSmallAccountBuilder().number(null).build();
        expectedException.expect(IllegalArgumentException.class);
        manager.deleteAccount(account);
    }
    
    @Test
    public void deleteAccountWithNonExistingNumber() {
        Account account = sampleSmallAccountBuilder().number(1L).build();
        expectedException.expect(IllegalEntityException.class);
        manager.deleteAccount(account);
    }
}