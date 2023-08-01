package uk.ac.kent.software_dev.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.kent.software_dev.src.DBManager;
import uk.ac.kent.software_dev.src.DBManager.Table;
import uk.ac.kent.software_dev.src.Util;
import uk.ac.kent.software_dev.src.Util.Constants;

@SuppressWarnings("unused")
public class DBManagerTest {
    private static final String TEST_PATIENT_FORENAME = "John Percival";
    private static final String TEST_PATIENT_SURNAME = "Hackworth";
    private static final String TEST_PATIENT_EMAIL = "oogabooga@stoneage.com";
    private static final String TEST_PATIENT_PASSWORD = "password420eyyy";
    private static final String TEST_PATIENT_DOB = "2000-03-12";
    private static final int TEST_PATIENT_ID = 1;
    private static final Character TEST_PATIENT_GENDER = 'M';
    private static final String ENTERED_PATIENT_FORENAME = "Bilbo";
    private static final String ENTERED_PATIENT_SURNAME = "Baggins";
    private static final String ENTERED_PATIENT_EMAIL = "BBaggins@shire.co.me";
    private static final String ENTERED_PATIENT_PASSWORD = "HobbitPipe";
    private static final String ENTERED_PATIENT_DOB = "1980-03-12";
    private static final Character ENTERED_PATIENT_GENDER = 'M';
    private static final int ENTERED_PATIENT_ID = 2;
    private static final int PREREG_PATIENT_ID = 0;
    private static final String PREREG_PATIENT_NAME = "Cosmo De Bonis-Campbell";
    private static final String PREREG_PATIENT_EMAIL = "cd586@kent.ac.uk";
    private static final String PREREG_PATIENT_PASSWORD = "Nosani 000";
    private static final int TEST_DOCTOR_ID = 0;
    private static final String AV_TEST_DOCTOR_FNAME = "John";
    private static final String AV_TEST_DOCTOR_SNAME = "Doe";
    private static final int UNAV_TEST_DOCTOR_ID = 1;
    private static final String UNAV_TEST_DOCTOR_FNAME = "Edgar";
    private static final String UNAV_TEST_DOCTOR_SNAME = "Allen Poe";
    private static final String APP_DATE = "2022-03-30";
    private static final String NEW_APP_DATE = "2022-04-30";
    private static final String APP_TIME = "14:00:00";
    private static final String APP_REASON = "Reason";
    private static final int TEST_BOOKING_ID = 1;
    private static final String VALID_TEST_UPDATE_DATE = "2022-03-31";
    private static final String TEST_UPDATE_TIME = "14:00:00";
    private static final String INVALID_TEST_DATE = "2022-03-29";
    private static final String VALID_DOCTOR_AV_DATE = "2022-04-4";
    public static DBManager databaseManager;
    public static Connection connection;

    // TODO Refactor into classes for testing database config, DBManager and
    // validation separately

    @BeforeClass
    public static void beforeAll() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            databaseManager = new DBManager();
            connection = DriverManager.getConnection(Constants.DATABASE_SERVER, Constants.DATABASE_USER,
                    Constants.DATABASE_PASSWORD);
        } catch (final SQLException e) {
            e.printStackTrace();
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Before
    public void before() {
        cleanupRegistration();
        cleanupAppointments();
        cleanupAvDoctor();
        cleanupUnavDoctor();
        cleanupPatient();
    }

    @Before
    public void inputDoctors() throws SQLException {
        final PreparedStatement st1 = connection
                .prepareStatement("INSERT INTO Doctor(did, forename, surname) VALUES (?, ?, ?)");
        st1.setInt(1,TEST_DOCTOR_ID);
        st1.setString(2, AV_TEST_DOCTOR_FNAME);
        st1.setString(3, AV_TEST_DOCTOR_SNAME);
        st1.executeUpdate();

        final PreparedStatement st2 = connection
                .prepareStatement("INSERT INTO Doctor(did, forename, surname) VALUES (?, ?, ?)");
        st2.setInt(1,UNAV_TEST_DOCTOR_ID);
        st2.setString(2, UNAV_TEST_DOCTOR_FNAME);
        st2.setString(3, UNAV_TEST_DOCTOR_SNAME);
        st2.executeUpdate();
    }
    @Before
    public void inputPatient() throws SQLException {
        final PreparedStatement st = connection
                .prepareStatement(
                        "INSERT INTO Patient(pid, forename, surname, dob, gender, doctor) VALUES (?, ?, ?, ?, ?, ?);");
        st.setInt(1, ENTERED_PATIENT_ID);
        st.setString(2, ENTERED_PATIENT_FORENAME);
        st.setString(3, ENTERED_PATIENT_SURNAME);
        st.setDate(4, Date.valueOf(ENTERED_PATIENT_DOB));
        st.setString(5, ENTERED_PATIENT_GENDER.toString());
        st.setInt(6, TEST_DOCTOR_ID);
        st.executeUpdate();
    }

    @Before
    public void inputBooking() throws SQLException {
        final PreparedStatement st = connection
                .prepareStatement(
                        "INSERT INTO Booking(bid, reason, date, time, patient, doctor) VALUES (?, ?, ?, ?, ?, ?);");
                st.setInt(1, TEST_BOOKING_ID);
                st.setString(2, APP_REASON);
                st.setDate(3, Date.valueOf(APP_DATE));
                st.setTime(4, Time.valueOf(APP_TIME));
                st.setInt(5, TEST_PATIENT_ID);
                st.setInt(6, UNAV_TEST_DOCTOR_ID);
                st.executeUpdate();
    }


    @Test
    public void testConnection() {
        if (databaseManager == null) {
            fail("Connection the the database failed. Further tests will inevitably fail");
        }
    }

    @Test
    public void testRegisteredLogin() throws SQLException, NoSuchAlgorithmException {
        assertTrue("Test user is not preregistered.",
                databaseManager.login(PREREG_PATIENT_EMAIL, PREREG_PATIENT_PASSWORD));
    }

    @Test
    public void testRegisteredDoctor() throws SQLException {
        final ResultSet doctors = databaseManager.getDoctors();
        assertTrue("There is no doctor with ID 0. Further tests will inevitably fail",
                (doctors.next() && doctors.getInt("did") == TEST_DOCTOR_ID));
    }

    @Test
    public void testIncorrectLogin() {
        try {
            assertFalse("Login to test user with incorrect details succeeded",
                    databaseManager.login(PREREG_PATIENT_EMAIL, "y92;,'1["));
        } catch (final SQLException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testNonexistantLogin() throws NoSuchAlgorithmException {
        try {
            assertFalse("Login with nonexistant details succeeded", databaseManager.login("TEST_EMAIL", "y92;,'1["));
        } catch (final SQLException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testCreateValidLogin() {
        try {
            final int pid = databaseManager.registerPatient(TEST_PATIENT_EMAIL, TEST_PATIENT_FORENAME,
                    TEST_PATIENT_SURNAME,
                    TEST_PATIENT_DOB, TEST_PATIENT_GENDER, TEST_DOCTOR_ID, TEST_PATIENT_PASSWORD, null);
            assertTrue("Registration of test user failed",
                    databaseManager.login(TEST_PATIENT_EMAIL, TEST_PATIENT_PASSWORD));
            databaseManager.unregisterPatient(pid, TEST_PATIENT_PASSWORD);
        } catch (final SQLIntegrityConstraintViolationException e1) {
            e1.printStackTrace();
            attemptRegistrationCleanup();
        } catch (final SQLException e2) {
            e2.printStackTrace();
            attemptRegistrationCleanup();
        } catch (final NoSuchAlgorithmException e) {
            e.printStackTrace();
            attemptRegistrationCleanup();
        }
    }
    @Test
    public void createValidAppointment() throws SQLException {
        try{
        databaseManager.bookAppointment(TEST_PATIENT_ID, NEW_APP_DATE, APP_TIME, APP_REASON, TEST_DOCTOR_ID);
            final PreparedStatement st = connection
                    .prepareStatement("SELECT FROM Booking WHERE patient =? AND doctor =?");
                st.setInt(1, TEST_PATIENT_ID);
                st.setInt(2, TEST_DOCTOR_ID);
                assertTrue("Valid appointment not created", st.executeQuery().next());

        } catch (final SQLIntegrityConstraintViolationException e1) {
            e1.printStackTrace();
            attemptBookingCleanup();
        }
    }

    @Test
    public void getValidAppointment() throws SQLException {
        try( ResultSet rs = databaseManager.getAppointments(TEST_PATIENT_ID,TEST_DOCTOR_ID, APP_DATE))
            {
                assertTrue("Valid appointment not found in table", rs.next());
                } catch (final SQLIntegrityConstraintViolationException e1) {
                e1.printStackTrace();
                attemptBookingCleanup();
            }
    }

    @Test
    public void updateValidAppointment() throws SQLException {
        try{
            databaseManager.updateAppointment(TEST_BOOKING_ID, UNAV_TEST_DOCTOR_ID, NEW_APP_DATE, TEST_UPDATE_TIME);
            final PreparedStatement st = connection
                    .prepareStatement("SELECT FROM Booking WHERE bid =? AND doctor =?");
            st.setInt(1, TEST_BOOKING_ID);
            st.setInt(2, UNAV_TEST_DOCTOR_ID);
            assertTrue("Valid appointment not created", st.executeQuery().next());
        } catch (final SQLIntegrityConstraintViolationException e1) {
            e1.printStackTrace();
            attemptBookingCleanup();
        }
    }
    @Test
    public void updateValidDoctor() throws SQLException {
        try {
            databaseManager.updateDoctor(TEST_DOCTOR_ID, ENTERED_PATIENT_ID);
            final PreparedStatement st = connection
                    .prepareStatement("SELECT FROM Patient WHERE pid =? AND doctor =?");
            st.setInt(1, ENTERED_PATIENT_ID);
            st.setInt(2, TEST_DOCTOR_ID);
            assertTrue("Doctor not updated", st.executeQuery().next());
        } catch (final SQLIntegrityConstraintViolationException e1) {
            e1.printStackTrace();
            attemptBookingCleanup();
        }
    }
    @Test
    public void getValidDoctor() throws SQLException {
        try( ResultSet rs = databaseManager.getDoctors())
        {
            assertTrue("Valid appointment not found in table", rs.next());
        } catch (final SQLIntegrityConstraintViolationException e1) {
            e1.printStackTrace();
            attemptBookingCleanup();
        }
    }


    @Test
    public void testMessage() {
        try {
            final int mid1 = databaseManager.recordMessage(PREREG_PATIENT_ID, null, Constants.PATIENT_REG_MSG);
            final int mid2 = databaseManager.recordMessage(null, TEST_DOCTOR_ID,
                    String.format(Constants.DOCTOR_NEW_PATIENT_MSG, PREREG_PATIENT_NAME));
            assertTrue("System failed to send new patient a confirmation message",
                    messagesContain(
                            databaseManager.getMessageStrings(PREREG_PATIENT_ID, Table.Patient),
                            Constants.PATIENT_REG_MSG));
            assertTrue("System failed to send doctor a new patient message",
                    messagesContain(
                            databaseManager.getMessageStrings(TEST_DOCTOR_ID, Table.Doctor),
                            String.format(Constants.DOCTOR_NEW_PATIENT_MSG, PREREG_PATIENT_NAME)));
            databaseManager.unrecordMessage(mid1);
            databaseManager.unrecordMessage(mid2);
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testValidForename() {
        assertTrue("Valid surname failed validation", Util.validateName(TEST_PATIENT_FORENAME, false));
    }

    @Test
    public void testInvalidForename() {
        assertFalse("Invalid surname passed validation", Util.validateName("John-Chri$topher", false));
    }

    @Test
    public void testValidSurname() {
        assertTrue("Valid surname failed validation", Util.validateName(TEST_PATIENT_SURNAME, true));
    }

    @Test
    public void testInvalidSurname() {
        assertFalse("Invalid surname passed validation", Util.validateName("Smith_Jones", true));
    }

    @Test
    public void testValidEmail() {
        assertTrue("Valid email failed validation", Util.validateEmail(TEST_PATIENT_EMAIL));
    }

    @Test
    public void testInvalidEmail() {
        assertFalse("Invalid email passed validation", Util.validateEmail("298r8vqvchxmh-"));
    }

    @Test
    public void testValidPassword() {
        assertTrue("Valid password failed validation", Util.checkPasswordStrength("Password 01"));
    }

    @Test
    public void testInvalidPassword() {
        assertFalse("Invalid password passed validation", Util.checkPasswordStrength("password"));
    }

    @AfterClass
    public static void afterAll() throws SQLException {
        if (databaseManager != null) {
            clearMessagesAfter(2);
            databaseManager.closeConnection();
        }
    }

    /**
     * Deletes messages that were sent in the last specified span of time
     * 
     * @param seconds The number of seconds to delete messages over
     * @throws SQLException If a database access error occurs
     */
    private static void clearMessagesAfter(final int seconds) throws SQLException {
        if (seconds > 60)
            return;
        final PreparedStatement st = connection
                .prepareStatement(String.format("SELECT mid FROM Message WHERE datetime > NOW() - %d;", seconds));
        final ResultSet results = st.executeQuery();
        final ArrayList<Integer> mids = new ArrayList<>();
        while (results.next()) {
            mids.add(results.getInt("mid"));
        }
        final PreparedStatement deleteMsg = connection.prepareStatement("DELETE FROM Message WHERE mid=?;");
        for (final Integer mid : mids) {
            deleteMsg.setInt(1, mid);
            deleteMsg.executeUpdate();
        }
    }

    /**
     * Convenience method to check if an array of messages contains a search string
     * 
     * @param string The string to search for
     * @return Whether the messages contain this string
     */
    private static boolean messagesContain(final String[] messages, final String string) {
        boolean contains = false;
        for (int i = 0; i < messages.length; i++) {
            contains = messages[i].contains(string) ? true : contains;
        }
        return contains;
    }

    /**
     * Convenience method to unregister test patients after a test or during one if
     * an error occurs (to prevent future errors)
     * 
     * @return Whether cleanup was successful
     * @throws NoSuchAlgorithmException If SHA-256 is not available in the
     *                                  environment
     */
    private static boolean cleanupRegistration() {
        try (PreparedStatement st = connection.prepareStatement("SELECT pid FROM Patient WHERE email=?;")) {
            st.setString(1, TEST_PATIENT_EMAIL);
            final ResultSet patients = st.executeQuery();
            final Integer pid = patients.next() ? patients.getInt("pid") : null;
            if (pid != null) {
                databaseManager.unregisterPatient(pid, TEST_PATIENT_PASSWORD);
                return true;
            } else {
                return true;
            }
        } catch (final SQLException e1) {
            e1.printStackTrace();
            fail("Attempted to unregister test patient but failed");
            return false;
        } catch (final NoSuchAlgorithmException e) {
            e.printStackTrace();
            fail(e.getMessage());
            return false;
        }
    }
    /**
     * Convenience method to delete test appointments after a test or during one if
     * an error occurs (to prevent future errors)
     *
     * @return Whether cleanup was successful
     */
    private static boolean cleanupAppointments(){
        try (PreparedStatement st = connection.prepareStatement("SELECT * FROM Booking WHERE pid=? AND did =?;")) {
            st.setInt(1, TEST_PATIENT_ID);
            st.setInt(2, TEST_DOCTOR_ID);

            final ResultSet bookings = st.executeQuery();
            final Integer bid = bookings.next() ? bookings.getInt("bid") : null;
            if (bid != null) {
                databaseManager.deleteAppointment(bid);
                return true;
            } else {
                return true;
            }
        } catch (final SQLException e1) {
            e1.printStackTrace();
            fail("Attempted to delete test appointment but failed");
            return false;
        }
    }
    private static boolean cleanupAvDoctor() {
        try (PreparedStatement st = connection.prepareStatement("SELECT * FROM Doctor WHERE did =?;")) {
            st.setInt(1, TEST_DOCTOR_ID);

            final ResultSet doctors = st.executeQuery();
            final Integer did = doctors.next() ? doctors.getInt("did") : null;
            if (did != null) {
                st.executeUpdate();
                return true;
            } else {
                return true;
            }
        } catch (final SQLException e1) {
            e1.printStackTrace();
            fail("Attempted to delete available test doctor but failed");
            return false;
        }
    }
    private static boolean cleanupUnavDoctor() {
        try (PreparedStatement st = connection.prepareStatement("SELECT * FROM Doctor WHERE did =?;")) {
            st.setInt(1, UNAV_TEST_DOCTOR_ID);

            final ResultSet doctors = st.executeQuery();
            final Integer did = doctors.next() ? doctors.getInt("did") : null;
            if (did != null) {
                st.executeUpdate();
                return true;
            } else {
                return true;
            }
        } catch (final SQLException e1) {
            e1.printStackTrace();
            fail("Attempted to delete unavailable test doctor but failed");
            return false;
        }
    }
    private static boolean cleanupPatient(){
        try (PreparedStatement st = connection.prepareStatement("SELECT * FROM Patient WHERE pip =?;")) {
            st.setInt(1, ENTERED_PATIENT_ID);

            final ResultSet patients = st.executeQuery();
            final Integer pip = patients.next() ? patients.getInt("pip") : null;
            if (pip != null) {
                st.executeUpdate();
                return true;
            } else {
                return true;
            }
        } catch (final SQLException e1) {
            e1.printStackTrace();
            fail("Attempted to delete test patient but failed");
            return false;
        }
    }




    /**
     * Convenience method to wrap registration methods within tests
     */
    private void attemptRegistrationCleanup() {
        if (cleanupRegistration()) {
            fail("An SQLException occured, successfully prevented debris from being left in the database");
        } else {
            fail("An SQLException occured, debris may have been left in the Patient and/or Login tables of the database");
        }
    }
    private void attemptBookingCleanup() {
        if (cleanupAppointments()) {
            fail("An SQLException occured, successfully prevented debris from being left in the database");
        } else {
            fail("An SQLException occured, debris may have been left in the booking table of the database");
        }
    }
    private void attemptDoctorAvCleanup(){
        if(cleanupAvDoctor()){
            fail("An SQLException occured, successfully prevented debris from being left in the database");
        } else {
            fail("An SQLException occured, debris may have been left in the Doctor table of the database");
        }
    }
    private void attemptDoctorUnavCleanup(){
        if(cleanupUnavDoctor()){
            fail("An SQLException occured, successfully prevented debris from being left in the database");
        } else {
            fail("An SQLException occured, debris may have been left in the Doctor table of the database");
        }
    }
    private void attemptPatientCleanup(){
        if(cleanupPatient()){
            fail("An SQLException occured, successfully prevented debris from being left in the database");
        } else {
            fail("An SQLException occured, debris may have been left in the Doctor table of the database");
        }
    }

    }
