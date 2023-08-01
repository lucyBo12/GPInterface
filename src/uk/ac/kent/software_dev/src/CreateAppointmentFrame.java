package uk.ac.kent.software_dev.src;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import uk.ac.kent.software_dev.src.Util.ComponentFactory;
import uk.ac.kent.software_dev.src.Util.Constants;


/**
 * Create Appointment Window
 * allows the user to create an appointment with a doctor
 */
public class CreateAppointmentFrame extends JFrame {

	private final DBManager databaseManager;
	private final ArrayList<DoctorSelection> doctors;
	private final String loggedInName;
    private final int loggedInID;

	/**
	 * Constructor for objects of type CreateAppointmentFrame. Sets up a connection to the
	 * database, exiting if this is not possible. Then sets up the window and the
	 * components inside of it
	 */
	public CreateAppointmentFrame(final DBManager databaseManager, final int loggedInID) {
        this.databaseManager = databaseManager;
        this.loggedInID = loggedInID;
		this.loggedInName = databaseManager.getPatientName(loggedInID);

		this.doctors = databaseManager.getDoctorList();


		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setTitle("Create Appointment");

		final JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(6, 6, 6, 6));
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		setContentPane(contentPane);
		addComponents(contentPane);

		this.pack();
		this.setVisible(true);
	}

	/**
	 * Creates and adds all components to the content pane
	 * 
	 * @param contentPane Content pane of the window
	 */
	public void addComponents(final Container contentPane) {
        contentPane.add(ComponentFactory.makeLabelPanel("Book appointment", ComponentFactory.HEADING_FONT, true));

		final DataEntryArea dateArea = new DataEntryArea(new JFormattedTextField(Constants.DATE_FORMAT), "Enter date: ", Constants.DATE_FORMAT.toPattern()) {
			@Override
			protected boolean inputValid(final String input) {
				return !input.isBlank();
			}};
		contentPane.add(dateArea);

		final DataEntryArea timeArea = new DataEntryArea(new JFormattedTextField(Constants.TIME_FORMAT), "Enter time: ", Constants.TIME_FORMAT.toPattern()) {
			@Override
			protected boolean inputValid(final String input) {
				return !input.isBlank();
			}};
		contentPane.add(timeArea);
		
		final DataEntryArea doctorArea = new DataEntryArea(ComponentFactory.makeComboBox(
                this.doctors.toArray(new DoctorSelection[this.doctors.size()])), "Doctor: ", null) {
            @Override
            protected boolean inputValid(final String input) {
                return true;
            }
        };
        contentPane.add(doctorArea);

		final DataEntryArea reasonArea = new DataEntryArea(ComponentFactory.makeBigTextField(this), "Reason: ", "Enter a reason") {
			@Override
			protected boolean inputValid(final String input) {
				return !input.isBlank();
			}};
		contentPane.add(reasonArea);

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 2));
		buttonPanel.add(makeCreateAppointmentButton(dateArea, timeArea, doctorArea, reasonArea));
		buttonPanel.add(ComponentFactory.makeCancelButton(this, databaseManager, loggedInID));
		buttonPanel.setAlignmentX(JButton.LEFT_ALIGNMENT);
		contentPane.add(buttonPanel);
	}

    /**
     * Creates a button to create an appointment with a doctor
     * @return Create  Appointment button
     */
    private JButton makeCreateAppointmentButton(final DataEntryArea dateArea, final DataEntryArea timeArea, final DataEntryArea doctorArea, final DataEntryArea reasonArea) {
		final JFrame thiz = this;
        final JButton createAppointmentButton = new JButton("Create Appointment");
        createAppointmentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final String date = dateArea.validateInput() ? (String) dateArea.getContents() : null;
				final String time = timeArea.validateInput() ? (String) timeArea.getContents() : null;
				final DoctorSelection doctor = doctorArea.validateInput() ? (DoctorSelection) doctorArea.getContents() : null;
				final Integer doctorID = doctor != null ? doctor.getID() : null;
				final String reason = reasonArea.validateInput() ? (String) reasonArea.getContents() : null;
				final List<Object> notNull = Arrays.asList(date, time, doctorID, reason);
				if(notNull.contains(null)) {
					thiz.pack();
					return;
				}
				try {
					databaseManager.bookAppointment(loggedInID, date, time, reason, doctorID);
					databaseManager.recordMessage(null, doctorID, String.format(Constants.DOCTOR_NEW_APTMNT_MSG, loggedInName, date));
					databaseManager.recordMessage(loggedInID, null, String.format(Constants.PATIENT_NEW_APTMNT_MSG, doctor.toString(), date));
				} catch (final SQLException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(thiz, Constants.DB_COMM_ERROR);
				}
            }
        });
        return createAppointmentButton;
    }
}