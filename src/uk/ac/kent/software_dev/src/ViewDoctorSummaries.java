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
 * Window for viewing doctor details
 */
//TODO should have more details like phoneNo
public class ViewDoctorSummaries extends JFrame {
    private final DBManager databaseManager;

    public ViewDoctorSummaries(final DBManager databaseManager, final int loggedInID) {
        this.databaseManager = databaseManager;

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Past Appointments");

        final JPanel contentPane = new JPanel();
        this.setContentPane(contentPane);
        this.setLayout(new BorderLayout());

        final JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new GridLayout(1, 1));
        tablePanel.add(new JScrollPane(getDoctorsTable()));
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
     * Method to create and populate a table containing the doctors details
     * @return the table to be displayed containing the doctors details
     */
    private JTable getDoctorsTable() {
        final String[] columnHeadings = { "Doctor ID", "Doctor Forename", "Doctor Surname"};
        try (ResultSet appointments = databaseManager.getDoctors()) {
            String[] columns = {"did", "forename", "surname"};
            Object[][] objectTable = databaseManager.getTable(columns, appointments);
            return ComponentFactory.makeTable(columnHeadings, objectTable);
        } catch (final SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, Constants.DB_COMM_ERROR);
        }
        return new JTable();
    }
}
