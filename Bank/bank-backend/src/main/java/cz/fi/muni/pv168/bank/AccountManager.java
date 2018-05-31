package cz.fi.muni.pv168.bank;

import java.util.List;


/**
 *
 * @author Katarina Matusova, Aneta Moravcikova
 */
public interface AccountManager {
    
    /** Stores new account into database. Number for the new account is automatically
     * generated and stored into number attribute.
     *
     * @param account account to be created.
     */
    public void createAccount(Account account);
    
    /** Returns account with given number.
     *
     * @param number primary key of requested account.
     * @return account with given number or null if such account does not exist.
     */
    public Account getAccount(Long number);

    /** Updates account in database.
     *
     * @param accForUpdate updated account to be stored into database.
     */
    public void updateAccount(Account accForUpdate);

    /** Deletes account from database.
     *
     * @param account account to be deleted from database.
     */
    public void deleteAccount(Account account);
        
    /**
     * Returns list of all accounts in the database.
     * @return list of all accounts in database.
     */
    public List<Account> findAllAccounts();
}
