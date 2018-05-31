package cz.fi.muni.pv168.bank;



import java.util.Objects;

/**
 * This entity class represents Account. Account has a number, note and balance.
 * Account can have only one note. All parameters are mandatory.
 * @author Aneta Moravcikova
 */
public class Account {
    
    private Long number;
    private String note;
    private double balance;
    
    public Long getNumber(){
        return number;
    }
    
    public void setNumber(Long number) {
        this.number = number;
    }

    public String getNote(){
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public double getBalance(){
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
    
    @Override
    public String toString() {
        return "Account{ number=" + number
                + ", note=" + note
                + ", balance=" + balance
                + "}";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Account other = (Account) obj;
        if (obj != this && this.number == null) {
            return false;
        }
        return Objects.equals(this.number, other.number);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.number);
    }

    

}

