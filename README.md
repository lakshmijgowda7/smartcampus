# Smart Campus Management System

A console-based Java application for managing campus data and resources.

## Architecture

- `Main.java` provides the interactive command-line menus.
- `StudentService`, `FacultyService`, and `CourseService` implement management operations and validate requests.
- `StudentDao`, `FacultyDao`, and `CourseDao` perform database reads and writes through JDBC.
- `DatabaseConnection` reads connection settings from environment variables; `DatabaseException` reports database errors.
- `Student`, `Faculty`, and `Course` are the domain models. `ManagementOperations` defines the common service contract.
- `database-schema.sql` creates the database and tables.
- `FileManager.java` provides CSV file utilities. The student/faculty/course application uses MySQL; the CSV files in `data/` are retained as project data/templates.
- `ResourceService` is a second console entry point for accounts, resources, bookings, and campus activities. Its services keep data in memory for the current run.
- `UserAuthService` handles in-memory accounts and password hashing; `CampusActivityService` and `ResourceBookingService` handle their respective operations.
- The `*Test.java` files are executable test programs for the resource, booking, authentication, and activity modules.

## Requirements

- Java Development Kit (JDK)
- MySQL Server
- MySQL Connector/J on the Java classpath

## Setup and run

1. Create the database and tables by executing `database-schema.sql` with MySQL Workbench or the MySQL command-line client.
2. Set the database connection environment variables. For PowerShell:

   ```powershell
   $env:SMART_CAMPUS_DB_URL = "jdbc:mysql://localhost:3306/smart_campus"
   $env:SMART_CAMPUS_DB_USER = "your_mysql_user"
   $env:SMART_CAMPUS_DB_PASSWORD = "your_mysql_password"
   ```

3. Compile from the project directory. To run the student/faculty/course application, replace the connector path with the location of your MySQL Connector/J JAR:

   ```powershell
   javac -cp ".;C:\path\to\mysql-connector-j.jar" *.java
   java -cp ".;C:\path\to\mysql-connector-j.jar" Main
   ```

The resource and activity application can be started without the database using `java ResourceService`. After compiling, run the included test programs with:

```powershell
java CampusActivityServiceTest
java ResourceBookingServiceTest
java ResourceServiceTest
java UserAuthServiceTest
```

Do not commit database credentials. The application reads them from the environment at runtime.
