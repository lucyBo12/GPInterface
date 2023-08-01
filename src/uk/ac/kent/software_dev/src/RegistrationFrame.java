package uk.ac.kent.software_dev.src;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;

import uk.ac.kent.software_dev.src.Util.ComponentFactory;
import uk.ac.kent.software_dev.src.Util.Constants;

/**
 * A window for creating a new account. Shows warnings when entered details are
 * invalid.
 */
public class RegistrationFrame extends JFrame {

    /**
     * Represents a possible seelction of a gender inside a JComboBox in this
     * window. Needed becuase registration queries must be sent with a gender's
     * single letter, but the user must be displayed some text
     */
    private enum GenderSelection {
        Male('M'), Female('F'), Other('O'), PreferNotToSay(null);

        private final Character letter;

        GenderSelection(final Character letter) {
            this.letter = letter;
        }

        @Override
        public String toString() {
            switch (this) {
                case PreferNotToSay:
                    return "Prefer not to say";
                default:
                    return this.name();
            }
        }

        public Character getLetter() {
            return this.letter;
        }
    }

    private final DBManager databaseManager;
    private ArrayList<DoctorSelection> doctors = new ArrayList<>();

    /**
     * Set up the window and its contents
     * 
     * @param databaseManager The instance of DBManager that this window will use to
     *                        communicate with the database
     */
    public RegistrationFrame(final DBManager databaseManager) {
        this.databaseManager = databaseManager;
        
        this.doctors = databaseManager.getDoctorList();
        
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Registration");

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

        final JPanel headingPanel = ComponentFactory.makeLabelPanel("Register new account", ComponentFactory.HEADING_FONT, true);
        headingPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        contentPane.add(headingPanel);

        final JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(1, 2));
        final JLabel info = ComponentFactory.makeLabel("Please provide the following details:", ComponentFactory.LABEL_FONT, false);
        info.setForeground(Color.red);
        infoPanel.add(info);
        infoPanel.add(Box.createGlue());
        infoPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        contentPane.add(infoPanel);

        final DataEntryArea forenameArea = new DataEntryArea("First name:",
                String.format(
                        "Name must be composed of less than or equal to %d alphabetic (or \" \" or \"-\") characters",
                        Constants.FORENAME_LENGTH)) {
            @Override
            protected boolean inputValid(final String input) {
                return Util.validateName(input, false);
            }
        };
        contentPane.add(forenameArea);

        final DataEntryArea surnameArea = new DataEntryArea("Surname:",
                String.format(
                        "Surname must be composed of less than or equal to %d alphabetic (or \" \" or \"-\") characters",
                        Constants.SURNAME_LENGTH)) {
            @Override
            protected boolean inputValid(final String input) {
                return Util.validateName(input, true);
            }
        };
        contentPane.add(surnameArea);

        final DataEntryArea emailArea = new DataEntryArea("Email:", String
                .format("Email must be in a valid format and less than %d characters long", Constants.EMAIL_LENGTH)) {
            @Override
            protected boolean inputValid(final String input) {
                return Util.validateEmail(input);
            }
        };
        contentPane.add(emailArea);

        final DataEntryArea passwordArea = new DataEntryArea(new JPasswordField(), "Password:",
                "Password must be more than 10 characters long and contain a capital letter, two numbers and a non-alphanumeric character") {
            @Override
            protected boolean inputValid(final String input) {
                return Util.checkPasswordStrength(input);
            }
        };
        contentPane.add(passwordArea);

        final DataEntryArea passwordConfirmArea = new DataEntryArea(new JPasswordField(), "Confirm password:", null) {
            @Override
            protected boolean inputValid(final String input) {
                return true;
            }
        };
        contentPane.add(passwordConfirmArea);

        final DataEntryArea dobArea = new DataEntryArea(new JFormattedTextField(Constants.DATE_FORMAT), "Date of birth:",
                Constants.DATE_FORMAT.toPattern()) {
            @Override
            protected boolean inputValid(final String input) {
                return !input.isBlank();
            }
        };
        contentPane.add(dobArea);

        final GenderSelection[] genders = { GenderSelection.Male, GenderSelection.Female, GenderSelection.Other,
                GenderSelection.PreferNotToSay };
        final DataEntryArea genderArea = new DataEntryArea(ComponentFactory.makeComboBox(genders), "Gender: ", null) {
            @Override
            protected boolean inputValid(final String input) {
                return true;
            }
        };
        contentPane.add(genderArea);

        final DataEntryArea disabilityArea = new DataEntryArea(ComponentFactory.makeBigTextField(this), "Please note any disabilities:",
                null) {
            @Override
            protected boolean inputValid(final String input) {
                return !input.isBlank();
            }
        };
        contentPane.add(disabilityArea);

        final DataEntryArea doctorArea = new DataEntryArea(ComponentFactory.makeComboBox(
                this.doctors.toArray(new DoctorSelection[this.doctors.size()])), "Doctor: ", null) {
            @Override
            protected boolean inputValid(final String input) {
                return true;
            }
        };
        contentPane.add(doctorArea);

        contentPane.add(new JPanel());

        final JPanel buttonPanel = new JPanel();
        final JButton newAccountButton = makeNewAccountButton(emailArea, forenameArea, surnameArea, dobArea,
                genderArea, doctorArea, passwordArea, passwordConfirmArea, disabilityArea);
        buttonPanel.add(newAccountButton);
        buttonPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        contentPane.add(buttonPanel);
    }

    /**
     * Creates a button that will use a DBManager to try to register a patient with
     * the entered details onto the database
     * 
     * @param emailArea      The field where the user entered their email
     * @param forenameArea   The field where the user entered their forename
     * @param surnameArea    The field where the user entered their surname
     * @param dobArea        The field where the user entered their date of birth
     * @param doctorField    The field where the user entered their doctor
     * @param passwordArea   The field where the user entered their password
     * @param disabilityArea The field where the user entered any disabilities
     * @return The button
     */
    private JButton makeNewAccountButton(final DataEntryArea emailArea, final DataEntryArea forenameArea,
            final DataEntryArea surnameArea,
            final DataEntryArea dobArea, final DataEntryArea genderArea,
            final DataEntryArea doctorArea,
            final DataEntryArea passwordArea,
            final DataEntryArea passwordConfirmArea,
            final DataEntryArea disabilityArea) {
        final JFrame thiz = this;
        final JButton newAccountButton = new JButton("Create Account");
        newAccountButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                final String email = emailArea.validateInput() ? (String) emailArea.getContents() : null;
                final String forename = forenameArea.validateInput() ? (String) forenameArea.getContents() : null;
                final String surname = surnameArea.validateInput() ? (String) surnameArea.getContents() : null;
                final String dob = dobArea.validateInput() ? (String) dobArea.getContents() : null;
                final String password = passwordArea.validateInput() ? (String) passwordArea.getContents() : null;
                final String passwordConfirm = passwordConfirmArea.validateInput()
                        ? (String) passwordConfirmArea.getContents()
                        : null;
                final String disabilities = disabilityArea.validateInput() ? (String) disabilityArea.getContents()
                        : null;
                final List<String> notNull = Arrays.asList(email, forename, surname, dob, password);

                final DoctorSelection doctorSelection = (DoctorSelection) doctorArea.getContents();
                final Integer doctor = doctorSelection != null ? doctorSelection.getID() : null;

                final GenderSelection genderSelection = (GenderSelection) genderArea.getContents();
                final Character gender = genderSelection != null ? genderSelection.getLetter() : null;

                if (notNull.contains(null)) {
                    thiz.pack();
                    return;
                } else if (!password.equals(passwordConfirm)) {
                    JOptionPane.showMessageDialog(thiz, "Your confirmed password is not the same");
                    return;
                } else if (doctor == null) {
                    JOptionPane.showMessageDialog(thiz, "You must select a doctor");
                    return;
                }

                try {
                    databaseManager.registerPatient(notNull.get(0), notNull.get(1), notNull.get(2), notNull.get(3),
                            gender, doctor, notNull.get(4), disabilities);
                    JOptionPane.showMessageDialog(thiz, "User registered!");
                    thiz.dispose();
                } catch (final SQLException e1) {
                    if (e1.getMessage().contains("email")) {
                        JOptionPane.showMessageDialog(thiz, "You already have an account, please try logging in");
                    } else if (e1.getMessage().contains("disabilities")) {
                        JOptionPane.showMessageDialog(thiz, "There is too much text in the disability field");
                    } else {
                        JOptionPane.showMessageDialog(thiz, Constants.DB_COMM_ERROR);
                    }
                } catch (final NoSuchAlgorithmException e1) {
                    JOptionPane.showMessageDialog(thiz,
                            "Cryptographic algorithms unavailable, cannot communicate securely");
                }
            }
        });
        return newAccountButton;
    }
}
