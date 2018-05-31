/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.bankgui;

import cz.fi.muni.pv168.bank.Person;
import cz.fi.muni.pv168.bank.PersonManager;
import cz.fi.muni.pv168.common.ServiceFailureException;
import java.time.LocalDate;
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
 * @author Katarina Matusova
 */
public class PersonTableModel extends AbstractTableModel {

    private volatile List<Person> people = new ArrayList<Person>();
    private static final Logger log = LoggerFactory.getLogger(PersonTableModel.class);

    private PersonManager manager;
    private final ResourceBundle bundle = ResourceBundle.getBundle("language", getLocale());

    public PersonTableModel(PersonManager manager) {
        log.debug("creating PersonTableModel");
        this.manager = manager;
        SwingWorker<List<Person>, Void> worker = new SwingWorker<List<Person>, Void>() {
            @Override
            protected List<Person> doInBackground() {
                try {
                    return manager.findAllPeople();
                } catch (Exception e) {
                    log.error("Error when finding all people", e);
                    JOptionPane.showMessageDialog(null, "Error when finding all people.");

                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    people = get();
                    super.done();
                    PersonTableModel.this.fireTableDataChanged();
                } catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(PersonTableModel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    java.util.logging.Logger.getLogger(PersonTableModel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        worker.execute();
    }

    @Override
    public int getRowCount() {
        return manager.findAllPeople().size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Person person = people.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return person.getId();
            case 1:
                return person.getName();
            case 2:
                return person.getBorn();
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    public void addPerson(Person person) {
        if (person != null) {
            SwingWorker<List<Person>, Void> worker = new SwingWorker<List<Person>, Void>() {
                @Override
                protected List<Person> doInBackground() {
                    try {
                        manager.createPerson(person);
                        return manager.findAllPeople();
                    } catch (ServiceFailureException e) {
                        log.error("Error when adding person", e);
                        JOptionPane.showMessageDialog(null, "Error when adding person .");
                    } catch (IllegalArgumentException e) {
                        log.error("Error when adding person", e);
                        JOptionPane.showMessageDialog(null, "Error when adding person .");

                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        people = get();
                        super.done();
                        PersonTableModel.this.fireTableDataChanged();
                    } catch (InterruptedException ex) {
                        java.util.logging.Logger.getLogger(PersonTableModel.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ExecutionException ex) {
                        java.util.logging.Logger.getLogger(PersonTableModel.class.getName()).log(Level.SEVERE, null, ex);
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
                return "Id";
            case 1:
                return bundle.getString("name");
            case 2:
                return bundle.getString("born");
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
                return String.class;
            case 2:
                return LocalDate.class;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Person person = people.get(rowIndex);
        switch (columnIndex) {
            case 1:
                if (verifyNameAndAlert((String) aValue)) {
                    person.setName((String) aValue);
                    SwingWorker<List<Person>, Void> worker = new SwingWorker<List<Person>, Void>() {
                        @Override
                        protected List<Person> doInBackground() {
                            try {
                                manager.updatePerson(person);
                                return manager.findAllPeople();
                            } catch (ServiceFailureException e) {
                                log.error("Error when setting value", e);
                                JOptionPane.showMessageDialog(null, "Error when setting value .");

                            } catch (IllegalArgumentException e) {
                                log.error("Error when setting value", e);
                                JOptionPane.showMessageDialog(null, "Error when setting value.");

                            }
                            return null;
                        }

                        @Override
                        protected void done() {
                            try {
                                people = get();
                                super.done();
                                PersonTableModel.this.fireTableDataChanged();
                            } catch (InterruptedException ex) {
                                java.util.logging.Logger.getLogger(PersonTableModel.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (ExecutionException ex) {
                                java.util.logging.Logger.getLogger(PersonTableModel.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        }
                    };
                    worker.execute();
                }
                break;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    public void removeRow(int row) {
        if (row < 0) {
            return;
        }

        Person person = getPerson(row);
        SwingWorker<List<Person>, Void> worker = new SwingWorker<List<Person>, Void>() {
            @Override
            protected List<Person> doInBackground() {
                try {
                    manager.deletePerson(person);
                    return manager.findAllPeople();
                } catch (ServiceFailureException e) {
                    log.error("Error removing row", e);
                    JOptionPane.showMessageDialog(null, "Error when removing row .");
                } catch (IllegalArgumentException e) {
                    log.error("Error removing row", e);
                    JOptionPane.showMessageDialog(null, "Error when removing row .");

                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    people = get();
                    super.done();
                    PersonTableModel.this.fireTableDataChanged();
                } catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(PersonTableModel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    java.util.logging.Logger.getLogger(PersonTableModel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        worker.execute();
    }

    public Person getPerson(int row) {
        return people.get(row);
    }

    public int getPersonIndex(Person person) {
        return people.indexOf(person);
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

    private boolean verifyNameAndAlert(String name) {
        if (name == null) {

            return false;
        }
        if (name.equals("")) {
            return false;
        }
        return true;
    }

}
