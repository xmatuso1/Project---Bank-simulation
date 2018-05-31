/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.bankgui;

import cz.fi.muni.pv168.bank.AccountManager;
import cz.fi.muni.pv168.bank.AccountManagerImpl;
import cz.fi.muni.pv168.bank.BankManager;
import cz.fi.muni.pv168.bank.BankManagerImpl;
import cz.fi.muni.pv168.bank.Person;
import cz.muni.fi.pv168.bankgui.AccountTableModel;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

/**
 *
 * @author Aneta Moravcikova
 */
public class AccountsFrame extends javax.swing.JFrame {
    
    private AccountManager accountManager;
    private BankManager bankManager;
    private final JFrame parent;
    private final Person owner;    
    private final ResourceBundle bundle = ResourceBundle.getBundle("language",getLocale());
    private static DataSource ds;    
    private final static org.slf4j.Logger log = LoggerFactory.getLogger(AccountsFrame.class);

    /**
     * Creates new form AccountsFrame
     * @param person
     * @param aThis
     * @param ds
     */

    public AccountsFrame(Person person, JFrame aThis, BankManager bM, AccountManager aM) {
        this.owner = person;
        this.parent = aThis;
        this.bankManager = bM;
        this.accountManager = aM;
        this.setTitle(person.getName());
        initComponents();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addAccount = new javax.swing.JButton();
        deleteAccount = new javax.swing.JButton();
        processPayment = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        accountsTable = new javax.swing.JTable();
        ownerName = new javax.swing.JLabel();

        setTitle(owner.getName());

        addAccount.setIcon(new javax.swing.ImageIcon(getClass().getResource("/addAccountBigger.png"))); // NOI18N
        addAccount.setText(bundle.getString("addAccount"));
        addAccount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addAccountActionPerformed(evt);
            }
        });

        deleteAccount.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deleteAccountBigger.png"))); // NOI18N
        deleteAccount.setText(bundle.getString("deleteAccount"));
        deleteAccount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteAccountActionPerformed(evt);
            }
        });

        processPayment.setIcon(new javax.swing.ImageIcon(getClass().getResource("/payment-icon.png"))); // NOI18N
        processPayment.setText(bundle.getString("processPayment"));
        processPayment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                processPaymentActionPerformed(evt);
            }
        });

        accountsTable.setModel(new AccountTableModel(bankManager, owner));
        jScrollPane1.setViewportView(accountsTable);

        ownerName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        ownerName.setText(owner.getName());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(addAccount, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deleteAccount)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(processPayment)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(ownerName))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 540, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addAccount, deleteAccount, processPayment});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(ownerName, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addAccount)
                    .addComponent(deleteAccount)
                    .addComponent(processPayment))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {addAccount, deleteAccount, processPayment});

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void deleteAccountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteAccountActionPerformed
        int column = 0;
        try {
            int row = accountsTable.getSelectedRow();
            Long number = (Long) accountsTable.getModel().getValueAt(row, column);
            AccountTableModel model = (AccountTableModel) accountsTable.getModel();
            model.removeRow(accountsTable.convertRowIndexToModel(row));
        } catch (Exception e) {
            log.error("Account not chosen");
            JOptionPane.showMessageDialog(rootPane, "You have to choose one account.");
        }
        AccountTableModel model = (AccountTableModel) accountsTable.getModel();
        model.fireTableDataChanged();
        
    }//GEN-LAST:event_deleteAccountActionPerformed

    private void addAccountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addAccountActionPerformed
       new AddAccountFrame(accountManager,this, owner, bundle).setVisible(true);
    }//GEN-LAST:event_addAccountActionPerformed

    private void processPaymentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_processPaymentActionPerformed
       new ProcessPaymentFrame(accountManager, bankManager, this, owner, bundle).setVisible(true);
    }//GEN-LAST:event_processPaymentActionPerformed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable accountsTable;
    private javax.swing.JButton addAccount;
    private javax.swing.JButton deleteAccount;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel ownerName;
    private javax.swing.JButton processPayment;
    // End of variables declaration//GEN-END:variables

    
    public JTable getTable(){
        return accountsTable;
    }

}
