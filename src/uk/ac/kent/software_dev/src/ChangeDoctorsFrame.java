package uk.ac.kent.software_dev.src;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import uk.ac.kent.software_dev.src.Util.ComponentFactory;
import uk.ac.kent.software_dev.src.Util.Constants;

/**
 * A window for viewing all doctors and providing the option to change doctors
 */
public class ChangeDoctorsFrame extends JFrame {

    private final int loggedInID;
    private final String loggedInName;
    private final DBManager databaseManager;
    private ArrayList<DoctorSelection> doctors = new ArrayList<>();

    /**
     * Set up the window and its contents
     * 
     * @param databaseManager The instance of DBManager that this window will use to
     *                        communicate with the database
     */
    public ChangeDoctorsFrame(final DBManager databaseManager, final int loggedInID) {
        this.databaseManager = databaseManager;
        this.loggedInID = loggedInID;
        this.loggedInName = databaseManager.getPatientName(loggedInID);

        this.doctors = databaseManager.getDoctorList();

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Change doctors");

        final JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(6, 6, 6, 6));
        this.setContentPane(contentPane);
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        addComponents(contentPane);

        this.pack();
        this.setResizable(false);
        this.setVisible(true);
    }

    /**
     * Set up the components of this window and add them to the frame
     * 
     * @param contentPane The content pane of this window
     */
    private void addComponents(final Container contentPane) {

        final JPanel headingPanel = ComponentFactory.makeLabelPanel("View doctors", ComponentFactory.HEADING_FONT, true);
        contentPane.add(headingPanel);

        final JPanel doctorPanel = new JPanel();
        final DataEntryArea doctorArea = new DataEntryArea(ComponentFactory.makeComboBox(
                this.doctors.toArray(new DoctorSelection[this.doctors.size()])), "Doctor: ", null) {
            @Override
            protected boolean inputValid(final String input) {
                return true;
            }
        };
        doctorPanel.add(doctorArea);
        contentPane.add(doctorPanel);

        contentPane.add(new JPanel());

        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2));
        buttonPanel.add(makeChangeDoctorButton(doctorArea));
        buttonPanel.add(ComponentFactory.makeCancelButton(this, databaseManager, loggedInID));
        contentPane.add(buttonPanel);
    }

    private JButton makeChangeDoctorButton(final DataEntryArea doctorArea) {
        final JButton changeDoctorButton = new JButton("Change Doctor");
        changeDoctorButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                final DoctorSelection doctorSelection = (DoctorSelection) doctorArea.getContents();
                final Integer doctorID = doctorSelection != null ? doctorSelection.getID() : null;
                try {
                    databaseManager.updateDoctor(loggedInID, doctorID);
                    databaseManager.recordMessage(loggedInID, null, String.format(Constants.PATIENT_NEW_DOCTOR_MSG, doctorSelection.toString()));
                    databaseManager.recordMessage(null, doctorID, String.format(Constants.DOCTOR_NEW_PATIENT_MSG, loggedInName));
                } catch (final SQLException e1) {
                    e1.printStackTrace();
                }
            }
        });
        return changeDoctorButton;
    }
}
