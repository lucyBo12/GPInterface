# Test Reports

The test classes for this project all run by first connecting to the database and setting up a test environment with the correct tables and some test tuples. The database is reset to a state of blank tables before and after each set of tests in order to ensure that neither the test tuples nor any side effects caused by the tests themselves persist in the database.

## Sprint 1

### Feature: login

#### Tests

- Connection test:
    - `testConnection()`
    - Tests the database schema can be connected to using the correct username and password.
    - Passed [x]

- Correct details test:
    - `testRegisteredLogin()`
    - Tests that the program allows login for a test patient, which should already be in the database. The test user has Cosmo's email address (`cd586@kent.ac.uk`) and the password is `nosani0` (the same as the database).
    - Passed [x]

- Preregistered doctor test:
    - `testRegisteredDoctor()`
    - Tests the doctor that the registration test depends on is already in the database - it always should be unless someone forgot to put it back after wiping the database.
    - Passed [x]

- Incorrect details test:
    - `testIncorrectLogin()`
    - Tests that the program does not allow login to the same user with a different password.
    - Passed [x]

- Nonexistant details test:
    -`textNonexistantLogin()`
    - Tests that the program does not allow login to a user which doesn't exist in the database.
    - Passed [x]

### Feature: registration

- Valid login registration test:
    - `testCreateValidLogin()`
    - Tests that the program does allow the user to register a login with valid details
    - Passed [x]

- Valid forename test:
    - `testValidForename()`
    - Tests that a valid forename passes validation
    - Passed [x]

- Invalid forename test:
    - `testsInvalidForename()`
    - tests that an invalid forename fails validation
    - Passed [x]

- Valid surname test:
    - `testValidSurname()`
    - Tests that a valid surname passes validation
    - Passed [x]

- Invalid surname test:
    - `testsInvalidSurname()`
    - tests that an invalid surname fails validation
    - Passed [x]

- Valid email test:
    - `testValidEmail()`
    - Tests that a valid email passes validation
    - Passed [x]

- Invalid email test:
    - `testsInvalidEmail()`
    - tests that an invalid email fails validation
    - Passed [x]

- Valid password test:
    - `testValidPassword()`
    - Tests that a secure password passes validation
    - Passed [x]

- Invalid password test:
    - `testsInvalidPassword()`
    - tests that an insecure password fails validation
    - Passed [x]