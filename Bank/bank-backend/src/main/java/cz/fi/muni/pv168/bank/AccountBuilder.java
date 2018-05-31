package cz.fi.muni.pv168.bank;

import cz.fi.muni.pv168.bank.Person;
import cz.fi.muni.pv168.bank.Account;
import cz.fi.muni.pv168.bank.Account;
import cz.fi.muni.pv168.bank.Person;



/**
 * This is builder for the Account class to make tests better readable.
 * @author Aneta moravcikova, uco 456444
 */
public class AccountBuilder {
    
    private Long number;
    private String note;
    private double balance;
    
    public AccountBuilder number(Long number) {
        this.number = number;
        return this;
    }
    
    public AccountBuilder note(String note) {
        this.note = note;
        return this;
    }
    
    public AccountBuilder balance(double balance) {
        this.balance = balance;
        return this;
    }
    
    public Account build() {
        Account account = new Account();
        account.setNumber(number);
        account.setNote(note);
        account.setBalance(balance);
        return account;
        
    }
}
