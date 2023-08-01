package uk.ac.kent.software_dev.src;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import uk.ac.kent.software_dev.src.DBManager.Table;
import uk.ac.kent.software_dev.src.Util.ComponentFactory;
import uk.ac.kent.software_dev.src.Util.Constants;

/**
 * Home Window
 * Displays user's messages and allows access to other features like creating
 * appointments, amending appointments, changing doctors, logging out.
 */
public class HomeFrame extends JFrame {

    private DBManager databaseManager;
    private final int loggedInID;

    /**
     * Constructor for objects of type HomeFrame. Sets up a connection to the
     * database, exiting if this is not possible. Then sets up the window and the
     * components inside of it
     */
    public HomeFrame(final DBManager databaseManager, final int pid) {

        this.databaseManager = databaseManager;
        this.loggedInID = pid;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Home");

        final JPanel contentPane = new JPanel();
        setContentPane(contentPane);
        this.setJMenuBar(makeMenuBar());
        final JTable messageTable = makeMessageTable(loggedInID);
        this.setLayout(new GridLayout(1, 1));
        this.add(new JScrollPane(messageTable));

        this.pack();
        // this.setResizable(false);
        this.setVisible(true);
    }

    /**
     * Make a menu bar that will contain all options for other screens/windows
     * 
     * @return The menu bar
     */
    private JMenuBar makeMenuBar() {
        final JFrame thiz = this;
        final JMenuBar menuBar = new JMenuBar();
        final JMenu file = new JMenu("File");
        final JMenuItem logout = new JMenuItem("Log out...");
        logout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    databaseManager.closeConnection();
                } catch (final SQLException e1) {
                    e1.printStackTrace();
                    databaseManager = null;
                }
                System.exit(0);
            }
        });
        file.add(logout);
        final JMenu edit = new JMenu("Edit");
        final JMenuItem doctor = new JMenuItem("Doctor...");
        doctor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                new ChangeDoctorsFrame(databaseManager, loggedInID);
                thiz.dispose();
            }
        });
        edit.add(doctor);
        final JMenu editAppointments = new JMenu("Appointments");
        final JMenuItem makeAppointment = new JMenuItem("Make appointment...");
        makeAppointment.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                new CreateAppointmentFrame(databaseManager, loggedInID);
                thiz.dispose();
            }
        });
        editAppointments.add(makeAppointment);
        final JMenuItem amendAppointment = new JMenuItem("Amend appointment...");
        amendAppointment.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                new AmendAppointmentFrame(databaseManager, loggedInID);
                thiz.dispose();
            }
        });
        editAppointments.add(amendAppointment);
        edit.add(editAppointments);
        final JMenu view = new JMenu("View");
        final JMenuItem viewAppointments = new JMenuItem("Booked appointments...");
        viewAppointments.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                new BookedAppointmentsFrame(databaseManager, loggedInID);
                thiz.dispose();
            }
        });
        view.add(viewAppointments);
        final JMenuItem viewPastAppointments = new JMenuItem("Past appointments...");
        viewPastAppointments.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                new PastAppointmentsFrame(databaseManager, loggedInID);
                thiz.dispose();
            }
        });
        view.add(viewPastAppointments);
        final JMenuItem viewDoctorSummaries = new JMenuItem("Doctor summaries...");
        viewDoctorSummaries.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                new DoctorSummariesFrame(databaseManager, loggedInID);
                thiz.dispose();
            }
        });
        view.add(viewDoctorSummaries);
        final JMenuItem viewDoctorDetails = new JMenuItem("Doctor details...");
        viewDoctorDetails.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                // TODO URGENT Launch window for viewing doctor details
                thiz.dispose();
            }
        });
        view.add(viewDoctorDetails);
        menuBar.add(file);
        menuBar.add(edit);
        menuBar.add(view);
        return menuBar;
    }

    /**
     * Make a table that will display all the messages for the logged in user
     * alongside their timestamps
     * 
     * @param loggedInID The pid of the currently logged in user
     * @return The table of messages
     */
    private JTable makeMessageTable(final int loggedinPID) {
        final String[] columnHeadings = { "Timestamp", "Message" };
        try (ResultSet messages = databaseManager.getMessages(loggedinPID, Table.Patient)) {
            String[] columns = {"datetime", "contains"};
            Object[][] objectTable = databaseManager.getTable(columns, messages);
            return ComponentFactory.makeTable(columnHeadings, objectTable);
        } catch (final SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, Constants.DB_COMM_ERROR);
        }
        return new JTable();
    }
}
