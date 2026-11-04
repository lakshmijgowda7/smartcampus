# Smart Campus Management System

A console-based Java application for managing students, faculty, and courses in a MySQL database.

## Architecture

- `Main.java` provides the interactive command-line menus.
- `StudentService`, `FacultyService`, and `CourseService` implement management operations and validate requests.
- `StudentDao`, `FacultyDao`, and `CourseDao` perform database reads and writes through JDBC.
- `DatabaseConnection` reads connection settings from environment variables; `DatabaseException` reports database errors.
- `Student`, `Faculty`, and `Course` are the domain models. `ManagementOperations` defines the common service contract.
- `database-schema.sql` creates the database and tables.
- `FileManager.java` provides CSV file utilities. The running application uses MySQL; the CSV files in `data/` are retained as project data/templates.

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

3. Compile and run from the project directory, replacing the connector path with the location of your MySQL Connector/J JAR:

   ```powershell
   javac -cp ".;C:\path\to\mysql-connector-j.jar" *.java
   java -cp ".;C:\path\to\mysql-connector-j.jar" Main
   ```

Do not commit database credentials. The application reads them from the environment at runtime.
