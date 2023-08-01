package uk.ac.kent.software_dev.src;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;

import uk.ac.kent.software_dev.src.Util.Constants;

/**
 * Loads JDBC and connects to the remote database, handling all communications
 * with it. Does not handle errors with the database for most methods
 * 
 * TODO Create setup (sub)class that reads .sql files to set up database
 */
public class DBManager {
	// private static final int HASHED_PASS_LENGTH = 32;
	private final Connection connection;

	/**
	 * Enum for keeping track of the ID column of the tables
	 */
	public enum Table {
		Patient("pid"), Doctor("did"), Message("mid"), Booking("bid");

		private String idColumn;

		Table(final String idColumn) {
			this.idColumn = idColumn;
		}

		public String getIDColumn() {
			return this.idColumn;
		}
	}

	/**
	 * Constructor for objects of type DBManager. Loads JDBC and tries to connect to
	 * the database
	 * 
	 * @throws SQLException If database login fails
	 */
	public DBManager() throws SQLException {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		connection = DriverManager.getConnection(Constants.DATABASE_SERVER, Constants.DATABASE_USER, Constants.DATABASE_PASSWORD);
		// System.out.println("Catch me if you can");
	}

	/**
	 * Creates and sends a login query to check if the specified email/password
	 * combination can log in
	 * 
	 * @param email    The email of the user trying to log in
	 * @param password The password of the user trying to log in
	 * @return Whether the user can log in
	 * @throws SQLException If a database access error occurs
	 */
	public boolean login(final String email, final String password) throws SQLException {
		String hashedPassword;
		try {
			hashedPassword = Util.getHash(password);
		} catch (final NoSuchAlgorithmException e) {
			e.printStackTrace();
			return false;
		}
		final PreparedStatement st = connection.prepareStatement(
				"SELECT * FROM Login, Patient WHERE Patient.email=? AND Login.pid=Patient.pid AND Login.hashed_pass=?;");
		st.setString(1, email);
		st.setString(2, hashedPassword);
		return st.executeQuery().next();
	}

	/**
	 * Get all the doctors currently in the database
	 * 
	 * @return The SQL results set containing the table of doctors
	 * @throws SQLException If a database access error occurs
	 */
	public ResultSet getDoctors() throws SQLException {
		final PreparedStatement st = connection.prepareStatement("SELECT * FROM Doctor;");
		final ResultSet results = st.executeQuery();
		return results;
	}

	public ArrayList<DoctorSelection> getDoctorList() {
		final ArrayList<DoctorSelection> doctorList = new ArrayList<>();
		try (ResultSet doctors = getDoctors()) {
			while (doctors.next()) {
				doctorList.add(new DoctorSelection(doctors.getInt("did"), doctors.getString("forename"),
						doctors.getString("surname")));
			}
		} catch (final SQLException e) {
			e.printStackTrace();
			return null;
		}
		return doctorList;
	}

	/**
	 * Attempts to register a patient into the database
	 * 
	 * @param email        The patient's email
	 * @param forename     The patient's forename
	 * @param surname      The patient's surname
	 * @param dob          The patient's date-of-birth
	 * @param doctor       The patient's doctor
	 * @param password     The patient's (hashed) password
	 * @param disabilities The patient's disabilities as entered by them
	 * @return The patient's generated unique ID
	 * @throws SQLException             If a database access error occurs
	 * @throws NoSuchAlgorithmException If SHA-256 is not available in the
	 *                                  environment
	 */
	public int registerPatient(final String email, final String forename, final String surname, final String dob,
			final Character gender,
			final int doctor,
			final String password,
			final String disabilities) throws SQLException, NoSuchAlgorithmException {
		final String hashedPassword = Util.getHash(password);
		final int pid = getNewID(Table.Patient);
		PreparedStatement st = connection.prepareStatement(
				"INSERT INTO Patient(pid, email, forename, surname, dob, gender, disabilities, doctor) VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
		st.setInt(1, pid);
		st.setString(2, email);
		st.setString(3, forename);
		st.setString(4, surname);
		st.setDate(5, Date.valueOf(dob));
		if (gender != null) {
			st.setString(6, Character.toString(gender));
		} else {
			st.setNull(6, Types.NULL);
		}
		st.setString(7, disabilities);
		st.setInt(8, doctor);
		st.executeUpdate();

		st = connection.prepareStatement("INSERT INTO Login(pid, hashed_pass) VALUES (?, ?);");
		st.setInt(1, pid);
		st.setString(2, hashedPassword);
		st.executeUpdate();

		recordMessage(pid, null, Constants.PATIENT_REG_MSG);
		recordMessage(null, doctor, String.format(Constants.DOCTOR_NEW_PATIENT_MSG, forename + " " + surname));

		return pid;
	}

	/**
	 * Unregisters the specified patient
	 * 
	 * @param pid      The pid of the patient
	 * @param password The patient's password for security
	 * @throws SQLException             If a database access error occurs
	 * @throws NoSuchAlgorithmException If SHA-256 is not available in the
	 *                                  environment
	 */
	public void unregisterPatient(final int pid, final String password) throws SQLException, NoSuchAlgorithmException {
		final String hashedPassword = Util.getHash(password);
		final ResultSet patient = connection.prepareStatement(
				String.format("SELECT pid, hashed_pass FROM Login WHERE pid=%d AND hashed_pass='%s';", pid,
						hashedPassword))
				.executeQuery();
		if (patient.next()) {
			connection.prepareStatement(String.format("DELETE FROM Patient WHERE pid=%d;", pid)).executeUpdate();
		}
	}

	/**
	 * Records to a patient or a doctor, possible from the other
	 * 
	 * @param patient The patient that the message concerns
	 * @param doctor  The doctor that the message concerns
	 * @param message The message
	 * @throws SQLException If a database access error
	 */
	public int recordMessage(final Integer patient, final Integer doctor, final String message) throws SQLException {
		final int mid = getNewID(Table.Message);
		final PreparedStatement st = connection.prepareStatement(
				"INSERT INTO Message(mid, datetime, contains, pid, did) VALUES (?, ?, ?, ?, ?);");
		st.setInt(1, mid);
		st.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
		st.setString(3, message);
		if (patient != null) {
			st.setInt(4, patient);
		} else {
			st.setNull(4, Types.NULL);
		}
		if (doctor != null) {
			st.setInt(5, doctor);
		} else {
			st.setNull(5, Types.NULL);
		}
		st.executeUpdate();
		return mid;
	}

	/**
	 * updates the patient's records for when they select a new doctor
	 * 
	 * @param patient The patient for which the doctor should be changed
	 * @param doctor  The doctor the patient is selecting
	 * @throws SQLException If a database acccess error occurs
	 */
	public void updateDoctor(final Integer patient, final Integer doctor) throws SQLException {
		final PreparedStatement st = connection.prepareStatement("UPDATE Patient SET doctor = ? WHERE pid = ?;");
		st.setInt(1, doctor);
		st.setInt(2, patient);
		st.executeUpdate();
	}

	/**
	 * Deletes a message from the system. Used by test classes
	 * 
	 * @param mid ID of message to be deleted
	 * @throws SQLException If a database access error occurs
	 */
	public void unrecordMessage(final int mid) throws SQLException {
		final PreparedStatement st = connection.prepareStatement(
				"DELETE FROM Message WHERE mid=?;");
		st.setInt(1, mid);
		st.executeUpdate();
	}

	/**
	 * Retrieve all the messages sent to the specified unique ID
	 * 
	 * @param id    The ID number
	 * @param table Which table this is ID is a primary key to - use DBManager
	 *              convenience constants
	 * @return The messages sent to this ID
	 * @throws SQLException If a database access error occurs
	 */
	public ResultSet getMessages(final int id, final Table table) throws SQLException {
		if (!(table == Table.Doctor || table == Table.Patient))
			return null;
		final String foreignKey = table.getIDColumn();
		if (foreignKey == null) {
			return null;
		}
		final PreparedStatement st = connection
				.prepareStatement(String.format("SELECT * FROM Message WHERE %s=?;", foreignKey));
		st.setInt(1, id);
		return st.executeQuery();
	}

	/**
	 * Retrieve all the messages sent to the specified unique ID
	 * 
	 * @param messages A set of rows from the message table containing their
	 *                 'contains' column
	 * @return The messages sent to this ID as strings
	 * @throws SQLException If a database access error occurs
	 */
	public String[] getMessageStrings(final int id, final Table table) throws SQLException {
		final ResultSet messages = getMessages(id, table);
		if (messages == null) {
			return null;
		}
		final ArrayList<String> messageStrings = new ArrayList<>();
		while (messages.next()) {
			messageStrings.add(messages.getString("contains"));
		}
		return messageStrings.toArray(new String[messageStrings.size()]);
	}

	/**
	 * Finds a unique ID for a new entity in a table
	 * 
	 * @param tableName The name of the table
	 * @param idColumn  The name of the column in the table where ID is stored
	 * @return A unique patient ID
	 * @throws SQLException If a database access error occurs
	 */
	private int getNewID(final Table table) throws SQLException {
		final String idColumn = table.getIDColumn();
		final PreparedStatement st = connection
				.prepareStatement(String.format("SELECT %s FROM %s;", idColumn, table.name()));
		final ResultSet results = st.executeQuery();
		final ArrayList<Integer> pids = new ArrayList<>();
		while (results.next()) {
			pids.add(results.getInt(idColumn));
		}
		if (!pids.isEmpty()) {
			pids.sort(null);
			return pids.get(pids.size() - 1) + 1;
		} else {
			return 0;
		}
	}

	/**
	 * Look up a set of appointments with the given constraining details
	 * 
	 * @param pid  The patient
	 * @param did  The doctor to look for appointments with, or null if this detail
	 *             not needed
	 * @param date The date of the appointment, or null if this detail is not needed
	 * @return A ResultSet containing all appointments with the details given
	 * @throws SQLException If a database access error occurs
	 */
	public ResultSet getAppointments(final int pid, final Integer did, final String date) throws SQLException {
		if (did != null) {
			final PreparedStatement st = connection
					.prepareStatement("SELECT * FROM Booking WHERE patient = ? AND doctor = ? AND date = ?");
			st.setInt(1, pid);
			st.setInt(2, did);
			st.setDate(3, Date.valueOf(date));
			return st.executeQuery();
		} else if (date != null) {
			final PreparedStatement st = connection.prepareStatement(
					"SELECT * FROM Booking, Doctor, Patient WHERE patient = ? AND date = ? AND Booking.doctor = did AND patient = pid;");
			st.setInt(1, pid);
			st.setDate(2, Date.valueOf(date));
			return st.executeQuery();
		} else {
			final PreparedStatement st = connection
					.prepareStatement("SELECT * FROM Booking, Doctor WHERE patient = ? AND did = doctor;");
			st.setInt(1, pid);
			return st.executeQuery();
		}
	}


	public boolean doctorAvailable(final int did, final String date, final String time) throws SQLException {
		final PreparedStatement st = connection.prepareStatement(
				"SELECT bid FROM Booking WHERE doctor = ? AND date = ? AND (time > SUBTIME(?, '00:59:00') AND (time < ADDTIME(?, '00:59:00')));");
		st.setInt(1, did);
		st.setDate(2, Date.valueOf(date));
		st.setTime(3, Time.valueOf(time));
		st.setTime(4, Time.valueOf(time));
		return !st.executeQuery().next();
	}

	/**
	 * 
	 * 
	 * @param bid
	 * @param did
	 * @param date The date of the new appointment in yyyy-mm-dd format
	 * @param time The time of the enw appointment in hh:mm format
	 * @throws SQLException
	 */
	public void updateAppointment(final int bid, final int did, final String date, final String time) throws SQLException {
		final PreparedStatement st = connection
				.prepareStatement("UPDATE Booking SET doctor = ?, date = ?, time = ? WHERE bid = ?;");
		st.setInt(1, did);
		st.setDate(2, Date.valueOf(date));
		st.setTime(3, Time.valueOf(time + ":00"));
		st.setInt(4, bid);
		st.executeUpdate();
	}

	public boolean canChangeAppointment(final int bid) throws SQLException {
		final PreparedStatement st = connection.prepareStatement("SELECT bid FROM Booking WHERE bid = ? AND (TIMESTAMP(date, time) BETWEEN NOW() AND TIMESTAMPADD(HOUR, 24, NOW()));");
		st.setInt(1, bid);
		return st.executeQuery().next();
	}

	public void deleteAppointment(final int bid) throws SQLException {
		final PreparedStatement st = connection.prepareStatement("DELETE FROM Booking WHERE bid = ?;");
		st.setInt(1, bid);
		st.executeUpdate();
	}

	/**
	 * 
	 * @param pid
	 * @param date
	 * @param time The time of the appointment in hh:mm format
	 * @param reason
	 * @param did
	 * @throws SQLException
	 */
	public void bookAppointment(final int pid, final String date, final String time, final String reason, final int did) throws SQLException {
		final PreparedStatement st = connection.prepareStatement(
				"INSERT INTO Booking(bid, reason, date, time, patient, doctor) VALUES (?, ?, ?, ?, ?, ?);");
		st.setInt(1, getNewID(Table.Booking));
		st.setString(2, reason);
		st.setDate(3, Date.valueOf(date));
		st.setTime(4, Time.valueOf(time + ":00"));
		st.setInt(5, pid);
		st.setInt(6, did);
		st.executeUpdate();
	}

	public Object[][] getTable(String[] columnNames, ResultSet queryResults) throws SQLException {
		final ArrayList<ArrayList<Object>> rows = new ArrayList<>();
		while (queryResults.next()) {
			final ArrayList<Object> columns = new ArrayList<>();
			for(String column : columnNames) {
				columns.add(queryResults.getObject(column));
			}
			rows.add(columns);
		}
		final int numRows = rows.size();
		final Object[][] objectTable = new Object[numRows][];
		for (int i = 0; i < numRows; i++) {
			objectTable[i] = rows.get(i).toArray();
		}
		return objectTable;
	}

	/**
	 * Find the pid of the patient with the given email address, or return null if
	 * there is none
	 * 
	 * @param email Email of the patient
	 * @return pid of the patient or null if there is none
	 * @throws SQLException If a database access error occurs
	 */
	public Integer getPID(final String email) throws SQLException {
		final PreparedStatement st = connection.prepareStatement("SELECT pid FROM Patient WHERE email=?;");
		st.setString(1, email);
		final ResultSet results = st.executeQuery();
		Integer pid;
		if (results.next()) {
			pid = results.getInt("pid");
		} else {
			pid = null;
		}
		return pid;
	}

	public String getPatientName(final int pid) {
		try (PreparedStatement st = connection.prepareStatement("SELECT forename, surname FROM Patient WHERE pid = ?;")) {
			st.setInt(1, pid);
			final ResultSet results = st.executeQuery();
			results.next();
			return results.getString("forename") + " " + results.getString("surname");
		} catch (final SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Fetches the logged in users appointments
	 * @return
	 * @throws SQLException
	 */
	public ResultSet getPastAppointments(int patient) throws SQLException {
		final PreparedStatement st = connection.prepareStatement("SELECT bid, time, visitDetails, prescriptions  FROM Appointment WHERE pid=?;");
		st.setInt(1, patient);
		final ResultSet results = st.executeQuery();
		return results;
	}



	/**
	 * Closes the connection to the database
	 * 
	 * @throws SQLException If a database access error occurs
	 */
	public void closeConnection() throws SQLException {
		connection.close();
	}
}