import java.sql.SQLException;

public class DatabaseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    static DatabaseException forConnection(SQLException exception) {
        if (isMissingDriver(exception)) {
            return new DatabaseException(
                    "MySQL JDBC driver is unavailable. Add the MySQL Connector/J JAR to the classpath.", exception);
        }
        return new DatabaseException(
                "Could not connect to MySQL. Check SMART_CAMPUS_DB_URL, user credentials, and server availability.",
                exception);
    }

    static DatabaseException forOperation(String operation, SQLException exception) {
        if (exception.getErrorCode() == 1062) {
            return new DatabaseException("A record with that ID or unique value already exists.", exception);
        }
        if (exception.getSQLState() != null && exception.getSQLState().startsWith("23")) {
            return new DatabaseException("The database rejected the record because of a constraint.", exception);
        }
        return new DatabaseException("Database error while " + operation + ": " + exception.getMessage(), exception);
    }

    private static boolean isMissingDriver(SQLException exception) {
        String message = exception.getMessage();
        return message != null && message.toLowerCase().contains("no suitable driver");
    }
}
