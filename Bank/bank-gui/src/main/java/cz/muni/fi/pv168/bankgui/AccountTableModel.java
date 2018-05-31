package cz.muni.fi.pv168.bankgui;

import cz.fi.muni.pv168.bank.Account;
import cz.fi.muni.pv168.bank.BankManager;
import cz.fi.muni.pv168.bank.Person;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Aneta Moravcikova
 */
public class AccountTableModel extends AbstractTableModel {

    private List<Account> accounts = new ArrayList<Account>();
    private BankManager manager;
    private Person owner;
    private final ResourceBundle bundle = ResourceBundle.getBundle("language", getLocale());
    private final static Logger log = LoggerFactory.getLogger(AccountTableModel.class);

    public AccountTableModel(BankManager manager, Person owner) {
        this.manager = manager;
        this.owner = owner;
        SwingWorker<List<Account>, Void> worker = new SwingWorker<List<Account>, Void>() {
            @Override
            protected List<Account> doInBackground() {
                try {
                   return manager.findAllAccountsByPerson(owner);
                } catch (Exception e) {
                    log.error("Error when getting accounts", e);
                    JOptionPane.showMessageDialog(null, "Error getting accounts");
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    accounts = get();
                    super.done();
                    AccountTableModel.this.fireTableDataChanged();
                } catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(AccountTableModel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    java.util.logging.Logger.getLogger(AccountTableModel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        worker.execute();
    }

    @Override
    public int getRowCount() {
        return accounts.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Account acc = accounts.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return acc.getNumber();
            case 1:
                return acc.getBalance();
            case 2:
                return acc.getNote();
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    public void addAccount(Account acc) {
        if (acc != null) {
            SwingWorker<List<Account>, Void> worker = new SwingWorker<List<Account>, Void>() {
                @Override
                protected List<Account> doInBackground() {
                    try {
                        manager.openNewAccount(acc, owner);
                        return manager.findAllAccountsByPerson(owner);
                    } catch (Exception e) {
                        log.error("No account added");
                        JOptionPane.showMessageDialog(null, "no account added");
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        accounts = get();
                        super.done();
                        AccountTableModel.this.fireTableDataChanged();
                        log.info("Account added");
                    } catch (InterruptedException ex) {
                        java.util.logging.Logger.getLogger(AccountTableModel.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ExecutionException ex) {
                        java.util.logging.Logger.getLogger(AccountTableModel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            worker.execute();
        }
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return bundle.getString("number");
            case 1:
                return bundle.getString("balance");
            case 2:
                return bundle.getString("note");
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Long.class;
            case 1:
                return double.class;
            case 2:
                return String.class;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        Account acc = accounts.get(rowIndex);
        switch (columnIndex) {
            case 0:
                acc.setNumber((Long) value);
                break;
            case 1:
                acc.setBalance((double) value);
                break;
            case 2:
                acc.setNote((String) value);
                break;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public Account getAccount(int row) {
        return accounts.get(row);
    }

    public int getAccountIndex(Account acc) {
        return accounts.indexOf(acc);
    }

    public void removeRow(int row) {
        if (row < 0) {
            return;
        }
        Account acc = getAccount(row);
        SwingWorker<List<Account>, Void> worker = new SwingWorker<List<Account>, Void>() {
            @Override
            protected List<Account> doInBackground() {
                try {
                    manager.deleteAccount(acc, owner);
                    return manager.findAllAccountsByPerson(owner);
                } catch (Exception e) {
                    log.error("Deleting account not succesfull", e);
                    JOptionPane.showMessageDialog(null, "Deleting account not successfull");
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    accounts = get();
                    super.done();
                    AccountTableModel.this.fireTableDataChanged();
                    log.info("Row removed");
                } catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(AccountTableModel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    java.util.logging.Logger.getLogger(AccountTableModel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        worker.execute();
    }

    public void processPayment(double sum, Account fromAcc, Account toAcc) {
        if (fromAcc != null & toAcc != null) {
            SwingWorker<List<Account>, Void> worker = new SwingWorker<List<Account>, Void>() {
                @Override
                protected List<Account> doInBackground() {
                    try {
                        manager.processPayment(sum, fromAcc, toAcc);
                        return  manager.findAllAccountsByPerson(owner);
                    } catch (Exception e) {
                        log.error("Process payment failed.");
                    JOptionPane.showMessageDialog(null, "Process payment failed");
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                    accounts = get();
                    super.done();
                    AccountTableModel.this.fireTableDataChanged();
                    log.info("Payment processed");
                } catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(AccountTableModel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    java.util.logging.Logger.getLogger(AccountTableModel.class.getName()).log(Level.SEVERE, null, ex);
                }
                }
            };
            worker.execute();
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 1:
                return true;
            case 2:
                return true;
            case 0:
                return false;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    public Locale getLocale() {
        return new Locale(System.getProperty("user.language"),
                System.getProperty("user.country"));
    }

}
