# GP Booking Patient Interface

A simple Java Swing GUI application for interacting with a GP booking system database, completed for Assessment 2 of module COMP5590 - Software Development at the University of Kent in Canterbury, Stage 2 Computer Science BSc.

The database is named `cd586` and is hosted at `dragon.kent.ac.uk` under username `cd586` with password temporarily set to `nosani0`.

__Deliverables__:
For each week can be found in `./docs/deliverables` as they are added.

- Scrum reports: `./docs/deliverables/scrum-reports.md`

- Test reports: `./docs/deliverables/test-reports.md`

- Database design: `./docs/deliverables/db-design.md`

- Quality assurance: `./docs/deliverables/quality-assurance.md`

- Group organisational plan below (kept on the repo front page for reference)

## Group member responsibilities:

#### Sam    - Database design

Designs the physical model of the database tables and writes SQL for constructing them and example tuples for tests.

#### Lucy   - OOP class design

Designs the OO model of the program's features, which attributes and functions will be needed for which classes.

#### Cosmo  - Project documentation and evidence organisation

Continuously collects and compiles documentation for deliverables. Also responsible for Javadoc.

#### Harry  - JUnit test design

Design JUnit tests on the class function level.

#### Ameen  - Quality control

Picks up TODOs and ensures the program functions as it is meant to. Encouraged to work on aesthetic features when nothing else is pressing.

## Sprint procedure:

In no particular order except as necessitated by dependency.
All design need ONLY be for those features being covered this sprint, but is always subject to revision.

- Decide on features to implement.

- Sam designs relevant relations in database.

- Lucy coordinates with Sam and designs relevant Java classes.

- Harry coordinates with Lucy and designs tests for these classes.

- We all use the above designs (which will be found in `./docs`) to build pieces of the features in our branches.

    - Remember the difference between merging __to__ `main` and merging __from__:

        - You will create a merge request __to__ `main` when you want to update main with code you have written (after you have committed it).

        - You will merge __from__ `main` when you want to update your branch with code other people have written. Always start your work sessions by doing this, and if you are working at the same time as other people, do it regularly while they are working as well (hopefully they will tell you and communicate what they are working on and what their progress is).

- Ameen is responsible for picking up any TODOs (made in Javadoc using `// TODO`) in the `main` branch, and ensuring the program functions as expected.

- Cosmo is continuously collecting documentation throughout this process and will compile the deliverables at the end of each sprint.

__*NOTE TO MARKER*__: This structure was quickly abandoned in reality. Strongly suggest using git blame to assess contributions to the project