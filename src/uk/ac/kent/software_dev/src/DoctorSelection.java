package uk.ac.kent.software_dev.src;

/**
 * Represents a possible selection of a doctor inside a JComboBox in this
 * window. Needed because registration queries must be sent with the doctor's
 * id, but the user must be displayed their name
 */
class DoctorSelection {
    private final String forename;
    private final String surname;
    private final int did;

    /**
     * Constructs a doctor selection
     * 
     * @param did      The doctor's unique ID
     * @param forename The doctor's forename
     * @param surname  The doctor's surname
     */
    public DoctorSelection(final int did, final String forename, final String surname) {
        this.did = did;
        this.forename = forename;
        this.surname = surname;
    }

    /**
     * Getter for the doctor's unique ID
     * 
     * @return The doctor's unique ID
     */
    public int getID() {
        return did;
    }

    /**
     * Ensures that only the doctor's name is displayed
     */
    @Override
    public String toString() {
        return "Dr. " + forename + " " + surname;
    }
}