# Smart Campus Management System

A Java console application for managing students, faculty, courses, resources, activities, and bookings.

## Architecture

- `Main.java` provides the interactive command-line menus.
- `StudentService`, `FacultyService`, and `CourseService` implement management operations and validate requests.
- `StudentDao`, `FacultyDao`, and `CourseDao` perform database reads and writes through JDBC.
- `DatabaseAuthService` authenticates administrators against MySQL using BCrypt; `AuthenticatedUser` carries the authenticated identity and role.
- `DatabaseConnection` reads connection settings from environment variables; `DatabaseException` reports database errors.
- `Student`, `Faculty`, and `Course` are the domain models. `ManagementOperations` defines the common service contract.
- `database-schema.sql` creates the basic database and tables. `database-auth-migration.sql` safely creates or adds the username column and unique index on the existing `users` table without changing existing password hashes or records.
- `FileManager.java` provides CSV file utilities. The student/faculty/course application uses MySQL; the CSV files in `data/` are retained as project data/templates.
- `Main.java` is the console entry point. The administrator dashboard gates the existing student, faculty, course, resource, activity, and booking menus.
- `ResourceService`, `CampusActivityService`, and `ResourceBookingService` provide resource, activity, and booking operations.
- The `*Test.java` files are executable test programs for the resource, booking, authentication, and activity modules.

## Requirements

- Java Development Kit (JDK)
- MySQL Server
- MySQL Connector/J on the Java classpath

## Setup and run

1. If needed, create the database and basic tables by executing `database-schema.sql`.
2. Apply the additive authentication migration to the existing database:

   ```powershell
   mysql -u your_mysql_user -p smart_campus_db < database-auth-migration.sql
   ```

   Existing `users` rows and password hashes are preserved. Legacy rows without usernames can log in with the Gmail local part as their username. First-administrator setup remains available until an enabled Gmail administrator is created.
3. Set the database connection environment variables. For PowerShell:

   ```powershell
   $env:SMART_CAMPUS_DB_URL = "jdbc:mysql://localhost:3306/smart_campus_db"
   $env:SMART_CAMPUS_DB_USER = "your_mysql_user"
   $env:SMART_CAMPUS_DB_PASSWORD = "your_mysql_password"
   ```

4. Compile and run from an interactive terminal. Provide the MySQL Connector/J and jBCrypt JAR paths:

   ```powershell
   javac -cp ".;C:\path\to\mysql-connector-j.jar;C:\path\to\jbcrypt-0.4.jar" *.java
   java -cp ".;C:\path\to\mysql-connector-j.jar;C:\path\to\jbcrypt-0.4.jar" Main
   ```

On first run, choose **Create First Administrator** and enter a username, Gmail address, and password of at least 12 characters. Password input is hidden. After setup, use **Administrator Login** to open the dashboard. Only accounts whose database role is `ADMIN` or `ADMINISTRATOR` can open it. `ResourceService` delegates to this same application entry point.

Run the included tests after compiling with:

```powershell
java -cp ".;C:\path\to\jbcrypt-0.4.jar" DatabaseAuthServiceTest
java CampusActivityServiceTest
java ResourceBookingServiceTest
java ResourceServiceTest
java UserAuthServiceTest
```

Do not commit database credentials or dependency JARs. The application reads credentials from the environment at runtime.
