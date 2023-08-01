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

public class DoctorSummariesFrame extends JFrame {
    private final DBManager databaseManger;

     /**
      * Creating frame and setting up everything.
      * @param databaseManager
      * @param loggedInID
      */
    public DoctorSummariesFrame(final DBManager databaseManager, final int loggedInID) {
        this.databaseManger = databaseManager;

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Doctor Summaries");

        final JPanel contentPane = new JPanel();
        this.setContentPane(contentPane);
        this.setLayout(new BorderLayout());

        final JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new GridLayout(1, 1));
        tablePanel.add(new JScrollPane(getDoctorSummariesTable()));
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
     * Creating table containing details of all doctors.
     * @return JTable containing Doctor summaries.
     */
    private JTable getDoctorSummariesTable() {
        final String[] columnHeadings = { "Doctor ID", "Forename", "Surname", "Phone Number"};
        try (ResultSet doctors = databaseManger.getDoctors()) {
            String[] columns = {"did", "forename", "surname", "phoneNumber"};
            Object[][] objectTable = databaseManger.getTable(columns, doctors);
            return ComponentFactory.makeTable(columnHeadings, objectTable);
        } catch (final SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, Constants.DB_COMM_ERROR);
        }
        return new JTable();
    }
}
