package uk.ac.kent.software_dev.src;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import uk.ac.kent.software_dev.src.Util.ComponentFactory;
import uk.ac.kent.software_dev.src.Util.Constants;

/**
 * A window for logging into the system. Also has an option for creating a new
 * account
 * 
 * TODO URGENT Admin log user
 * 
 * TODO Create main class for managing window classes
 * 
 * TODO Manage project dependencies using Maven
 */
public class LoginFrame extends JFrame {

	private DBManager databaseManager;

	/**
	 * The main method of the program. Opens a login window with the system's local
	 * look-and-feel.
	 * 
	 * @param args Argumnts run with the program. Ignored.
	 */
	public static void main(final String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		SwingUtilities.invokeLater(new Runnable() { // Start the GUI on the EDT
			public void run() {
				try {
					new LoginFrame();
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Constructor for objects of type LoginFrame. Sets up a connection to the
	 * database, exiting if this is not possible. Then sets up the window and the
	 * components inside of it
	 */
	private LoginFrame() {
		try {
			databaseManager = new DBManager();
		} catch (final SQLException e) {
			JOptionPane.showMessageDialog(this, "Database connection failed. Program exiting");
			System.exit(1);
		}

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setTitle("Login");

		final JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(6, 6, 6, 6));
		setContentPane(contentPane);
		contentPane.setLayout(new GridBagLayout());
		addComponents(contentPane);
		this.pack();
		this.setVisible(true);
	}

	/**
	 * Creates and adds all components to the content pane
	 * 
	 * @param contentPane Content pane of the window
	 */
	private void addComponents(final Container contentPane) {
		add(ComponentFactory.makeLabel("Login", ComponentFactory.HEADING_FONT, true), new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

		add(ComponentFactory.makeLabel("E-mail: ", ComponentFactory.LABEL_FONT, false), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

		add(ComponentFactory.makeLabel("Password: ", ComponentFactory.LABEL_FONT, false), new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

		final JTextField emailField = makeEmailField();
		add(emailField, new GridBagConstraints(1, 1, 3, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

		final JPasswordField passwordField = makePasswordField();
		add(passwordField, new GridBagConstraints(1, 2, 3, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

		add(makeShowPasswordBox(passwordField), new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

		add(makeLoginButton(emailField, passwordField), new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));

		add(makeNewAccountButton(), new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
	}

	/**
	 * Creates a button to open a window where the user can create a new account
	 * 
	 * @return The new account button
	 */
	private JButton makeNewAccountButton() {
		final JButton createNewAccountButton = new JButton("Register");
		createNewAccountButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				new RegistrationFrame(databaseManager);
			}
		});
		return createNewAccountButton;
	}

	/**
	 * Creates a button to ask the database if the user can login with entered
	 * details
	 * 
	 * @param emailField    The text field where the user entered their email
	 * @param passwordField The text field where the user entered their password
	 * @return The login button
	 */
	private JButton makeLoginButton(final JTextField emailField, final JPasswordField passwordField) {
		final JButton loginButton = new JButton("Login");
		loginButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				final String email = emailField.getText();
				final String password = String.valueOf(passwordField.getPassword());
				try {
					if (databaseManager.login(email, password)) {
						final int pid = databaseManager.getPID(email);
						dispose();
						new HomeFrame(databaseManager, pid);
					} else {
						JOptionPane.showMessageDialog(loginButton, "Wrong username & password");
					}
				} catch (final SQLException e1) {
					JOptionPane.showMessageDialog(loginButton, Constants.DB_COMM_ERROR);
				} catch (final HeadlessException e1) {
					e1.printStackTrace();
				}
			}
		});
		return loginButton;
	}

	/**
	 * Creates a check box to determine whether or not the password should be shown
	 * as it is typed in
	 * 
	 * @param passwordField The field where the password is being typed
	 * @return The check box
	 */
	private JCheckBox makeShowPasswordBox(final JPasswordField passwordField) {
		final JCheckBox showPassword = new JCheckBox("Show Password");
		showPassword.addActionListener(new ActionListener() {
			// If checkbox is ticked, password is visible, if not, it is hidden
			public void actionPerformed(final ActionEvent e) {
				if (showPassword.isSelected()) {
					passwordField.setEchoChar((char) 0);
				} else {
					passwordField.setEchoChar('•');
				}
			}
		});
		return showPassword;
	}

	/**
	 * Creates a field where the user can enter their password
	 * 
	 * @return The password field
	 */
	private JPasswordField makePasswordField() {
		final JPasswordField passwordField = new JPasswordField();
		return passwordField;
	}

	/**
	 * Creates a field where the user can enter their email
	 * 
	 * @return The email field
	 */
	private JTextField makeEmailField() {
		final JTextField emailField = new JTextField();
		return emailField;
	}
}