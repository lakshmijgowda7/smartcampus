import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {
    private static final String URL_VARIABLE = "SMART_CAMPUS_DB_URL";
    private static final String USER_VARIABLE = "SMART_CAMPUS_DB_USER";
    private static final String PASSWORD_VARIABLE = "SMART_CAMPUS_DB_PASSWORD";

    private DatabaseConnection() {
    }

    public static Connection getConnection() {
        String url = requiredSetting(URL_VARIABLE);
        String user = requiredSetting(USER_VARIABLE);
        String password = System.getenv(PASSWORD_VARIABLE);

        if (!url.startsWith("jdbc:mysql://")) {
            throw new DatabaseException(URL_VARIABLE + " must be a MySQL JDBC URL.");
        }
        if (password == null) {
            throw new DatabaseException("Set " + PASSWORD_VARIABLE + " in the environment.");
        }

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException exception) {
            throw DatabaseException.forConnection(exception);
        }
    }

    public static void testConnection() {
        try (Connection connection = getConnection()) {
            if (!connection.isValid(5)) {
                throw new DatabaseException("The MySQL connection did not pass its validity check.");
            }
        } catch (SQLException exception) {
            throw DatabaseException.forConnection(exception);
        }
    }

    private static String requiredSetting(String variableName) {
        String value = System.getenv(variableName);
        if (value == null || value.trim().isEmpty()) {
            throw new DatabaseException("Set " + variableName + " in the environment before starting the app.");
        }
        return value.trim();
    }
}
