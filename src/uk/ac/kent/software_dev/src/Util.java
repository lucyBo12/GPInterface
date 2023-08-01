package uk.ac.kent.software_dev.src;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;

public final class Util {
    public static class Constants {

        public static final String DATABASE_SERVER = "jdbc:mysql://dragon.kent.ac.uk/cd586";
        public static final String DATABASE_USER = "cd586";
        public static final String DATABASE_PASSWORD = "nosani0";

        public static final String DOCTOR_NEW_PATIENT_MSG = "You have a new patient: %s.";
        public static final String DOCTOR_APTMNT_CHANGE_MSG = "Your appointment with %s on %s changed to %s.";
        public static final String DOCTOR_NEW_APTMNT_MSG = "%s booked an appointment with you on %s.";
        public static final String DOCTOR_DEL_APTMNT_MSG = "%s cancelled their appointment with you on %s.";
        public static final String PATIENT_NEW_DOCTOR_MSG = "You changed your doctor to: %s.";
        public static final String PATIENT_APTMNT_CHANGE_MSG = "You changed your appointment on %s with %s to %s with %s.";
        public static final String PATIENT_NEW_APTMNT_MSG = "You booked an appointment with %s on %s.";
        public static final String PATIENT_REG_MSG = "Congragulations on your new account! You may now proceed to make an appointment.";
        public static final String PATIENT_DEL_APTMNT_MSG = "You deleted your appointment with %s on %s.";

        public static final String DB_COMM_ERROR = "Communication with the database failed";
        public static final String APTMNT_CHANGE_WARNING = "Sorry, but we do not allow appointments scheduled for less than 24 hours from now to be changed";

        public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
        public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

        public static final int FORENAME_LENGTH = 20;
        public static final int SURNAME_LENGTH = 40;
        public static final int EMAIL_LENGTH = 40;

    }

    public static class ComponentFactory {
        public static final Font HEADING_FONT = new Font("Helvetica Neue", Font.PLAIN, 20);
        public static final Font SUBHEADING_FONT = new Font("Helvetica Neue", Font.BOLD, 15);
        public static final Font LABEL_FONT = new Font("Helvetica Neue", Font.PLAIN, 12);
        
        /**
         * Make a table with the given column headings and row data
         * 
         * @param headings The column headings
         * @param table    The table data
         * @return         The table component
         */
        public static JTable makeTable(String[] headings, Object[][] objectTable) {
            JTable table = new JTable(objectTable, headings) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            return table;
        }

        /**
         * Creates a text area where patients can type their disabilities
         * 
         * @return The text area
         */
        public static JTextArea makeBigTextField(final JFrame parent) {
            final JTextArea field = new JTextArea(5, 30);
            field.setLineWrap(true);
            field.setFont(new Font("Helvetica", Font.PLAIN, 12));
            field.addComponentListener(new ComponentListener() {
                @Override
                public void componentResized(final ComponentEvent e) {
                    parent.pack();
                }
        
                @Override
                public void componentMoved(final ComponentEvent e) {
                }
        
                @Override
                public void componentShown(final ComponentEvent e) {
                }
        
                @Override
                public void componentHidden(final ComponentEvent e) {
                }
            });
            return field;
        }

        /**
         * Creates a combo box of objects of some type by taking a list of objects of
         * that type
         * 
         * @param <T>  The type of the objects
         * @param list The list of the objects
         * @return The combo box
         */
        public static <T> JComboBox<T> makeComboBox(final T[] list) {
            final JComboBox<T> field = new JComboBox<>(list);
            field.setSelectedIndex(-1);
            return field;
        }

        public static JLabel makeLabel(final String text, final Font font, final boolean center) {
            final JLabel label = new JLabel(text);
            if(font != null) label.setFont(font);
            if(center) label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            return label;
        }

        /**
         * Creates a panel containing some text
         * 
         * @param text The text to put in the text
         * @param font The font the text should be written in
         * @return A JPanel containing the text
         */
        public static JPanel makeLabelPanel(final String text, final Font font, final boolean center) {
            final JLabel label = Util.ComponentFactory.makeLabel(text, font, center);
            final JPanel headingPanel = new JPanel();
            headingPanel.add(label);
            headingPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
            return headingPanel;
        }

        public static JButton makeCancelButton(final JFrame parent, final DBManager databaseManager, final int pid) {
            final JButton cancel = new JButton("Cancel");
            cancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    parent.dispose();
                    new HomeFrame(databaseManager, pid);
                }});
            return cancel;
        }
    }

    public static int getComponentIndex(final Component component) {
        if (component != null && component.getParent() != null) {
            final Container c = component.getParent();
            for (int i = 0; i < c.getComponentCount(); i++) {
                if (c.getComponent(i) == component)
                    return i;
            }
        }
        return -1;
    }

    /**
	 * Uses a SHA-256 hashing algorithm to calculate hexadecimal hashes for password
	 * storage. NOT cryptographically secure as no salt etc.
	 * 
	 * TODO Make this cryptographically secure
	 * 
	 * @param input String to be hashed - usually the plaintext password
	 * @return 32-digit hexadecimal hash of the input string
	 * @throws NoSuchAlgorithmException If SHA-256 is not available in the
	 *                                  environment
	 */
	public static String getHash(final String input) throws NoSuchAlgorithmException {
		final MessageDigest sha = MessageDigest.getInstance("SHA-256");
		final byte[] hashBytes = sha.digest(input.getBytes(StandardCharsets.UTF_8));
		final BigInteger hash = new BigInteger(1, hashBytes);
		final StringBuilder hex = new StringBuilder(hash.toString(16));
		while (hex.length() < 64) {
			hex.insert(0, '0');
		}
		return hex.toString();
	}

    /**
	 * Check that email address is in a valid format and not too long for the
	 * database
	 * 
	 * @param email Email address
	 * @return Whether the email is valid
	 */
	public static boolean validateEmail(final String email) {
		return (email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$") && email.length() < Constants.EMAIL_LENGTH);
	}

    /**
	 * Check that a password is secure enough:
	 * Is more than 10 characters long and contains a capital, two numbers and a
	 * non-alphanumeric character
	 * 
	 * @param password The password to be checked
	 * @return Whether the password is secure enough
	 */
	public static boolean checkPasswordStrength(final String password) {
		if (password.length() < 10) {
			return false;
		}

		boolean secure = true;

		boolean cap = false;
		for (int i = 0; i < password.length(); i++) {
			if (Character.isUpperCase(password.charAt(i))) {
				cap = true;
				break;
			}
		}
		secure = !cap ? false : secure;

		int numbers = 0;
		for (int i = 0; i < password.length(); i++) {
			if (Character.isDigit(password.charAt(i))) {
				numbers++;
				if (numbers > 1) {
					break;
				}
			}
		}
		secure = numbers < 2 ? false : secure;

		boolean punct = false;
		for (int i = 0; i < password.length(); i++) {
			if (!(Character.isAlphabetic(password.charAt(i)) && Character.isDigit(password.charAt(i)))) {
				punct = true;
				break;
			}
		}
		secure = !punct ? false : secure;

		return secure;
	}

    /**
	 * Check that a name is made of sensible characters and will fit in the database
	 * 
	 * @param name Name to check
	 * @param surname Whether the name is a surname
	 * @return Whether the name is valid
	 */
	public static boolean validateName(final String name, final boolean surname) {
		final int length = surname ? Constants.SURNAME_LENGTH : Constants.FORENAME_LENGTH;
		return (name.length() <= length && name.matches("[a-zA-Z- ]+"));
	}
}
