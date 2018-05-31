package cz.fi.muni.pv168.bank;

import cz.fi.muni.pv168.common.DBUtils;
import cz.fi.muni.pv168.common.ServiceFailureException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * This service allows to manipulate with accounts.
 * @author Aneta moravcikova, uco 456444
 */
public class AccountManagerImpl implements AccountManager{
    
    private DataSource ds;
    
    private static final Logger logger = Logger.getLogger(AccountManagerImpl.class.getName());

    public AccountManagerImpl() {
    }
    
    public AccountManagerImpl(DataSource ds) {
        this.ds = ds;
    }

    public void setDataSource(DataSource ds) {
        this.ds = ds;
    }
    
    private void checkDataSource() {
        if (ds == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }

    @Override
    public void createAccount(Account account) {
        checkDataSource();
        validate(account);
        if (account.getNumber() != null) {
            throw new IllegalArgumentException("account number is already set");
        }
        Long number;
        Connection conn = null;
        PreparedStatement st;
        try {
            conn = ds.getConnection();
            st = conn.prepareStatement(
                    "INSERT INTO Account (note,balance) VALUES (?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            st.setString(1,account.getNote());
            st.setDouble(2, account.getBalance());
            
            int addedRows = st.executeUpdate();
            if (addedRows != 1) {
                throw new ServiceFailureException("Internal Error: More rows ("
                        + addedRows + ") inserted when trying to insert account " + account);
            }
            ResultSet keyRS = st.getGeneratedKeys();
            number = getKey(keyRS);
            account.setNumber(number);
        } catch (SQLException ex) {
            String msg = "Error when inserting account into db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            if(conn !=null){
                try {
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(AccountManagerImpl.class.getName()).log(Level.SEVERE, "Error closing connection: ", ex);
                }
            }
        }
        
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
    
    private void validate(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
    }
    
    @Override
    public Account getAccount(Long number) {
         checkDataSource();
        if (number == null) {
            throw new IllegalArgumentException("number is null");
        }
        
        try (Connection conn = ds.getConnection();
            PreparedStatement st = conn.prepareStatement(
                        "SELECT NUMBER, NOTE, BALANCE FROM ACCOUNT WHERE NUMBER = ?")) {
            st.setLong(1, number);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                Account result = new Account();
                result.setNumber(rs.getLong("number"));
                result.setNote( rs.getString("note"));
                result.setBalance(rs.getDouble("balance"));
                if (rs.next()) {
                    throw new ServiceFailureException(
                            "Internal error: More accounts with the same number found ");
                }
                return result;
            } else {
                return null;
            }
        } catch (SQLException ex) {
            String msg = "Error when getting account with number = " + number + " from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }
      
    }

    @Override
    public void updateAccount(Account accForUpdate) {
        checkDataSource();
        validate(accForUpdate);
        if (accForUpdate.getNumber() == null) {
            throw new IllegalArgumentException("account number is null.");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = ds.getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "UPDATE Account SET note = ?, balance = ? WHERE number = ?");
            st.setString(1, accForUpdate.getNote());
            st.setDouble(2, accForUpdate.getBalance());
            st.setLong(3, accForUpdate.getNumber());

            int count = st.executeUpdate();
            if (count == 0) {
                throw new IllegalArgumentException("Account " + accForUpdate + " was not found in database!");
            } else if (count != 1) {
                throw new ServiceFailureException("Invalid updated rows count detected (one row should be updated): " + count);
            }
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when updating account in the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            closeQ(conn, st);
        }
    }
        
    private static void closeQ (Connection conn, Statement ... statements) {
           
        if (conn != null) {
            try {
                if (conn.getAutoCommit()) {
                    throw new IllegalStateException("Connection is in the autocommit mode!");
                }
                conn.rollback();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error when doing rollback", ex);
            }
        }
        
        for (Statement st : statements) {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                    logger.log(Level.SEVERE, "Error when closing statement", ex);
                }                
            }
        }        
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error when switching autocommit mode back to true", ex);
            }
            try {
                conn.close();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Error when closing connection", ex);
            }
        }
        
    }

    @Override
    public void deleteAccount(Account account) {
        checkDataSource();
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }        
        if (account.getNumber() == null) {
            throw new IllegalArgumentException("account number is null");
        }        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = ds.getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "DELETE FROM Account WHERE number = ?");
            st.setLong(1, account.getNumber());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, account, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when deleting body from the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public List<Account> findAllAccounts() {
        try (
            Connection connection = ds.getConnection();
            PreparedStatement st = connection.prepareStatement(
                    "SELECT * FROM ACCOUNT")) {

            ResultSet rs = st.executeQuery();

            List<Account> result = new ArrayList<>();
            while (rs.next()) {
                Account account = new Account();
                account.setNote(rs.getString("NOTE"));
                account.setBalance(rs.getDouble("BALANCE"));
                account.setNumber(rs.getLong("NUMBER"));
                result.add(account);
            }
            return result;

        } catch (SQLException ex) {
            String msg = "Error when getting all bodies from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        }
    }
}