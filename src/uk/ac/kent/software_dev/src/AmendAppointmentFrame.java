package uk.ac.kent.software_dev.src;

import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import uk.ac.kent.software_dev.src.Util.ComponentFactory;
import uk.ac.kent.software_dev.src.Util.Constants;

/**
 * Window for changing existing appointments
 */
public class AmendAppointmentFrame extends JFrame {

    private final DBManager databaseManager;
    private DataEntryArea origAppointmentArea;
    private final String loggedInName;
    private final int loggedInID;

    private class AppointmentSelection {
        private final int BID;
        private final Date DATE;
        private final String DOCTOR;
        private final int DOCTORID;

        /**
         * Constructs a possible selection of an appointment in a ComboBox
         * 
         * @param bid     The booking ID
         * @param doctor  The doctor the appointments with
         * @param patient The patient
         * @param time    The times
         * @param doctorID
         */
        public AppointmentSelection(final int bid, final String doctor, final Date date, int doctorID) {
            this.BID = bid;
            this.DOCTOR = doctor;
            this.DATE = date;
            this.DOCTORID = doctorID;
        }

        public int getDID() {
            return DOCTORID;
        }

        /**
         * Getter for the appointment's unique ID
         * 
         * @return The booking unique ID
         */
        public int getID() {
            return BID;
        }

        /**
         * Getter for the appointment's date
         * 
         * @return The date of the appointment
         */
        public Date getDate() {
            return DATE;
        }

        /**
         * Ensures that only the time and doctors name is
         */
        @Override
        public String toString() {
            return "Dr. " + DOCTOR;
        }
    }

    /**
     * Constructor for objects of type CreateAppointmentFrame. Sets up a connection
     * to the
     * database, exiting if this is not possible. Then sets up the window and the
     * components inside of it
     */
    public AmendAppointmentFrame(final DBManager databaseManager, final int loggedInID) {
        this.databaseManager = databaseManager;
        this.loggedInID = loggedInID;
        this.loggedInName = databaseManager.getPatientName(loggedInID);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Amend appointment");

        final JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(6, 6, 6, 6));
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        this.setContentPane(contentPane);
        addComponents(contentPane);

        this.pack();
        this.setResizable(false);
        this.setVisible(true);
    }

    /**
     * Creates and adds all components to the content pane
     * 
     * @param contentPane Content pane of the window
     */
    private void addComponents(final Container contentPane) {
        final JFrame thiz = this;
        contentPane
                .add(ComponentFactory.makeLabelPanel("Amend appointment", new Font("Helvetica", Font.PLAIN, 20), true));

        contentPane.add(ComponentFactory.makeLabelPanel("Select the appointment you wish to amend:",
                new Font("Helvetica", Font.BOLD, 15), false));

        final JPanel origDatePanel = new JPanel();
        final DataEntryArea origDateArea = new DataEntryArea(new JFormattedTextField(Constants.DATE_FORMAT),
                "Enter date of appointment to change: ", Constants.DATE_FORMAT.toPattern()) {
            @Override
            protected boolean inputValid(final String input) {
                return !input.isBlank();
            }
        };
        origDatePanel.add(origDateArea);
        final JButton checkOrigButton = new JButton("Check");
        checkOrigButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (origDateArea.validateInput()) {
                    refreshAptmntSelector((String) origDateArea.getContents());
                }
                thiz.pack();
            }
        });
        origDatePanel.add(checkOrigButton);
        origDatePanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        contentPane.add(origDatePanel);

        final JComboBox<AppointmentSelection> origAppointmentSelector = new JComboBox<>();
        setOrigAppointmentArea(origAppointmentSelector);
        contentPane.add(origAppointmentArea);

        contentPane.add(ComponentFactory.makeLabelPanel("Select new appointment:", new Font("Helvetica", Font.BOLD, 15),
                false));

        final DataEntryArea newDateArea = new DataEntryArea(new JFormattedTextField(Constants.DATE_FORMAT),
                "Enter date of new appointment: ", Constants.DATE_FORMAT.toPattern()) {
            @Override
            protected boolean inputValid(final String input) {
                return !input.isBlank();
            }
        };
        contentPane.add(newDateArea);

        final DataEntryArea newTimeArea = new DataEntryArea(new JFormattedTextField(Constants.TIME_FORMAT),
                "Enter time of new appointment: ", Constants.TIME_FORMAT.toPattern()) {
            @Override
            protected boolean inputValid(final String input) {
                return !input.isBlank();
            }
        };
        contentPane.add(newTimeArea);

        final ArrayList<DoctorSelection> doctors = databaseManager.getDoctorList();
        final JComboBox<DoctorSelection> doctorSelectionBox = new JComboBox<>(
                doctors.toArray(new DoctorSelection[doctors.size()]));
        doctorSelectionBox.setSelectedIndex(-1);
        final DataEntryArea newDoctorArea = new DataEntryArea(doctorSelectionBox,
                "Select the doctor you would like the new appointment with: ", "Select one") {
            @Override
            protected boolean inputValid(final String input) {
                return !input.isBlank();
            }
        };
        contentPane.add(newDoctorArea);

        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3));
        buttonPanel.add(makeAmendButton(newDateArea, newTimeArea, newDoctorArea));
        buttonPanel.add(makeDeleteButton());
        buttonPanel.add(ComponentFactory.makeCancelButton(this, databaseManager, loggedInID));
        buttonPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        contentPane.add(buttonPanel);
    }

    private JButton makeAmendButton(final DataEntryArea newDateArea, final DataEntryArea newTimeArea,
            final DataEntryArea newDoctorArea) {
        final JFrame thiz = this;
        final JButton button = new JButton("Amend");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final AppointmentSelection origAppointment = origAppointmentArea.validateInput()
                        ? (AppointmentSelection) origAppointmentArea.getContents()
                        : null;
                final Integer bid = origAppointment == null ? null : origAppointment.getID();
                final String newDate = newDateArea.validateInput() ? (String) newDateArea.getContents() : null;
                final String newTime = newTimeArea.validateInput() ? (String) newTimeArea.getContents() : null;
                final DoctorSelection newDoctor = newDoctorArea.validateInput()
                        ? (DoctorSelection) newDoctorArea.getContents()
                        : null;
                final Integer newDoctorID = newDoctor == null ? null : newDoctor.getID();
                final List<Object> notNull = Arrays.asList(bid, newDate, newTime, newDoctorID);
                if (notNull.contains(null)) {
                    thiz.pack();
                    return;
                }

                try {
                    if (!databaseManager.doctorAvailable(newDoctorID, newDate, newTime)) {
                        JOptionPane.showMessageDialog(thiz,
                                "Your chosen doctor is not available on your chosen time and day. Please select another combination");
                        return;
                    }
                    if (!databaseManager.canChangeAppointment(bid)) {
                        JOptionPane.showMessageDialog(thiz, Constants.APTMNT_CHANGE_WARNING);
                        return;
                    }
                } catch (final SQLException e2) {
                    e2.printStackTrace();
                    JOptionPane.showMessageDialog(thiz, Constants.DB_COMM_ERROR);
                }
                try {
                    databaseManager.updateAppointment(bid, newDoctorID, newDate, newTime);
                    final String origDate = origAppointment.getDate().toLocalDate()
                            .format(DateTimeFormatter.ofPattern(Constants.DATE_FORMAT.toPattern()));
                    databaseManager.recordMessage(loggedInID, null, String.format(Constants.PATIENT_APTMNT_CHANGE_MSG,
                            origDate, origAppointment.toString(), newDate, newDoctor.toString()));
                    if (newDoctor.toString().equals(origAppointment.toString())) {
                        databaseManager.recordMessage(null, newDoctorID,
                                String.format(Constants.DOCTOR_APTMNT_CHANGE_MSG, loggedInName, origDate, newDate));
                    } else {
                        databaseManager.recordMessage(null, newDoctorID,
                                String.format(Constants.DOCTOR_NEW_APTMNT_MSG, loggedInName, newDate));
                        databaseManager.recordMessage(null, origAppointment.getDID(), String.format(Constants.DOCTOR_DEL_APTMNT_MSG, loggedInName, origDate));
                    }
                    JOptionPane.showMessageDialog(thiz, "Appointment changed");
                    thiz.dispose();
                    new HomeFrame(databaseManager, loggedInID);
                } catch (final SQLException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(thiz, Constants.DB_COMM_ERROR);
                }
            }
        });

        return button;
    }

    private JButton makeDeleteButton() {
        final JFrame thiz = this;
        JButton delete = new JButton("Delete");
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final AppointmentSelection appointment = origAppointmentArea.validateInput()
                        ? (AppointmentSelection) origAppointmentArea.getContents()
                        : null;
                final Integer appointmentID = appointment != null ? appointment.getID() : null;
                if (appointmentID == null) {
                    thiz.pack();
                    return;
                }
                try {
                    if (databaseManager.canChangeAppointment(appointmentID)) {
                        databaseManager.deleteAppointment(appointmentID);
                        String date = appointment.getDate().toLocalDate().format(
                            DateTimeFormatter.ofPattern(Constants.DATE_FORMAT.toPattern()));
                        databaseManager.recordMessage(loggedInID, null,
                                String.format(Constants.PATIENT_DEL_APTMNT_MSG, appointment.toString(), date));
                        databaseManager.recordMessage(null, appointment.getDID(), String.format(Constants.DOCTOR_DEL_APTMNT_MSG, loggedInName, date));
                        JOptionPane.showMessageDialog(thiz, "Appointment deleted");
                        thiz.dispose();
                        new HomeFrame(databaseManager, loggedInID);
                    }
                    else {
                        JOptionPane.showMessageDialog(thiz, Constants.APTMNT_CHANGE_WARNING);
                        return;
                    }
                } catch (SQLException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(thiz, Constants.DB_COMM_ERROR);
                }
            }
        });
        return delete;
    }

    private void refreshAptmntSelector(final String date) {
        final int pos = Util.getComponentIndex(origAppointmentArea);
        remove(origAppointmentArea);
        final JComboBox<AppointmentSelection> comboBox = new JComboBox<>(getAptmntSelections(date));
        comboBox.setSelectedIndex(-1);
        setOrigAppointmentArea(comboBox);
        add(origAppointmentArea, pos);
        this.revalidate();
        this.repaint();
    }

    private AppointmentSelection[] getAptmntSelections(final String date) {
        final ArrayList<AppointmentSelection> appointmentsList = new ArrayList<>();
        try (ResultSet appointments = databaseManager.getAppointments(loggedInID, null, date)) {
            while (appointments.next()) {
                final String doctorName = appointments.getString("Doctor.forename") + " "
                        + appointments.getString("Doctor.surname");
                final AppointmentSelection appointment = new AppointmentSelection(appointments.getInt("bid"),
                        doctorName, appointments.getDate("date"), appointments.getInt("Doctor.did"));
                appointmentsList.add(appointment);
            }
        } catch (final SQLException e) {
            e.printStackTrace();
            return appointmentsList.toArray(new AppointmentSelection[appointmentsList.size()]);
        }
        return appointmentsList.toArray(new AppointmentSelection[appointmentsList.size()]);
    }

    private void setOrigAppointmentArea(final JComboBox<AppointmentSelection> comboBox) {
        origAppointmentArea = new DataEntryArea(comboBox, "Select appointment to change: ", "Select one") {
            @Override
            protected boolean inputValid(final String input) {
                return !input.isBlank();
            }
        };
    }
}
