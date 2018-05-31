/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.fi.muni.pv168.bank;

import cz.fi.muni.pv168.common.ServiceFailureException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * This class implements BankManager service.
 * @author Aneta Moravcikova ,Katarina Matusova
 */

public class BankManagerImpl implements BankManager {
 
    private static final Logger logger = Logger.getLogger(
            PersonManagerImpl.class.getName());

    private DataSource dataSource;

    @Override
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }   


      
    @Override
    public List<Account> findAllAccountsByPerson(Person person) {
        checkDataSource();
        validatePerson(person);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement(
                    "SELECT NUMBER, NOTE, BALANCE " +
                    "FROM ACCOUNT JOIN PERSON ON ID = PERSONID " +
                    "WHERE ID = ?")) {
            
            st.setLong(1, person.getId());
            
            try (ResultSet rs = st.executeQuery()) {
            List<Account> result = new ArrayList<>();
            while (rs.next()) {
                Account account = new Account();
                account.setNumber(rs.getLong("NUMBER"));
                account.setNote(rs.getString("NOTE"));
                account.setBalance(rs.getDouble("BALANCE"));
                
                result.add(account);
            }
            return result;
        }
        } catch (SQLException ex) {
            String msg = "Error when trying to find accounts for person";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }
    }
    

    @Override
    public Person findPersonWithAccount(Account account) {
        checkDataSource();
        validateAccount(account);    
        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement(
                    "SELECT ID, NAME, BORN " +
                    "FROM PERSON JOIN ACCOUNT ON ID = PERSONID " +
                    "WHERE NUMBER = ?")) {

            st.setLong(1, account.getNumber());
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                Person person = new Person();
                person.setId(rs.getLong("ID"));
                person.setName(rs.getString("NAME"));
                person.setBorn(rs.getDate("BORN").toLocalDate());

                return person;
            } else {
                return null;
            }
        } catch (SQLException ex) {
            String msg = "Error when trying to find person with account " + account;
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }      
    }

    @Override
    public void openNewAccount(Account account, Person person) {
       checkDataSource();
        validatePerson(person);  
        validateAccount(account);
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("UPDATE ACCOUNT SET PERSONID = ? WHERE NUMBER = ? AND PERSONID IS NULL")) {
                
                conn.setAutoCommit(false);
                st.setLong(1, person.getId());
                st.setLong(2, account.getNumber());
                int count = st.executeUpdate();
                if (count == 0)
                    throw new IllegalArgumentException("Account " + account + " not found or it alreadz belong to person");
                if (count != 1)
                    throw new IllegalArgumentException("updated " + count + " instead of 1 account");
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when opening account to person", ex);
        }
    }
    
        @Override
    public void processPayment(double sum, Account from, Account to) {
        checkDataSource();
        validateAccount(from);
        validateAccount(to);
        if (sum <= 0) {
            throw new IllegalArgumentException("Sum is not positive");
        }
        
        
                try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement(
                    "UPDATE Account SET balance = CASE number  WHEN ? THEN ? WHEN ? THEN ? ELSE balance END WHERE number IN(?, ?)")) {
            
            double fromBalance = from.getBalance() - sum;
            double toBalance = to.getBalance() + sum;
           
            st.setLong(1, from.getNumber());
            st.setDouble(2, fromBalance);
            st.setLong(3, to.getNumber());
            st.setDouble(4, toBalance);
            st.setLong(5, from.getNumber());
            st.setLong(6, to.getNumber());
            
            int count = st.executeUpdate();
            if (count == 0) {
                throw new IllegalArgumentException("Account was not found in database!");
            } else if (count != 2) {
                throw new IllegalArgumentException("Invalid updated rows count detected (two rows should be updated): " + count);
            }
        } catch (SQLException ex) {
            String msg = "Error when process payment";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }
    }
  

     @Override
        public void deleteAccount(Account account, Person person) {
        checkDataSource();
        validatePerson(person);
        validateAccount(account);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement(
                     "UPDATE account SET personId = NULL WHERE number = ? AND personId = ?")) {
            st.setLong(1, account.getNumber());
            st.setLong(2, person.getId());
            int count = st.executeUpdate();
            if (count == 0) {
                throw new IllegalArgumentException("Account " + account + " was not found in database!");
            } else if (count != 1) {
                throw new IllegalArgumentException("Invalid updated rows count detected (one row should be updated): " + count);
            }  
        } catch (SQLException ex) {
            String msg = "Error when deleting account";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }
    }
    
    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }
    
    private void validatePerson(Person person) throws IllegalArgumentException {
        if (person == null) {
            throw new IllegalArgumentException("person is null.");
        }
        
        if (person.getId() == null) {
            throw new IllegalArgumentException("person id is null");
        }
    }
    
    private void validateAccount(Account account) throws IllegalArgumentException {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }        
        if (account.getNumber() == null) {
            throw new IllegalArgumentException("account number is null");
        }   
    }


    
}
