package uk.ac.kent.software_dev.src;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import uk.ac.kent.software_dev.src.Util.ComponentFactory;
import uk.ac.kent.software_dev.src.Util.Constants;

/**
 * Window for viewing booked appointments
 */
public class BookedAppointmentsFrame extends JFrame {
    private final DBManager databaseManager;
    private final int loggedinID;

    public BookedAppointmentsFrame(final DBManager databaseManager, final int loggedInID) {
        this.databaseManager = databaseManager;
        this.loggedinID = loggedInID;

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Appointments");

        final JPanel contentPane = new JPanel();
        this.setContentPane(contentPane);
        this.setLayout(new BorderLayout());

        final JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new GridLayout(1, 1));
        tablePanel.add(new JScrollPane(getAppointmentsTable()));
        contentPane.add(tablePanel, BorderLayout.CENTER);

        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        final JButton cancelButton = ComponentFactory.makeCancelButton(this, databaseManager, loggedInID);
        cancelButton.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        buttonPanel.add(cancelButton);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        this.pack();
        this.setVisible(true);
    }

    /**
     * Method for creating and populating a table of booked appointments
     * @return the populated JTable
     */
    private JTable getAppointmentsTable() {
        final String[] columnHeadings = { "Booking ID", "Reason", "Date", "Time", "Doctor Forename", "Doctor Surname" };
        try (ResultSet appointments = databaseManager.getAppointments(loggedinID, null, null)) {
            String[] columns = {"bid", "reason", "date", "time", "forename", "surname"};
            Object[][] objectTable = databaseManager.getTable(columns, appointments);
            return ComponentFactory.makeTable(columnHeadings, objectTable);
        } catch (final SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, Constants.DB_COMM_ERROR);
        }
        return new JTable();
    }
}
