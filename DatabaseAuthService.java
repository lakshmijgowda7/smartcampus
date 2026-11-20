import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.regex.Pattern;

import org.mindrot.jbcrypt.BCrypt;

public final class DatabaseAuthService {
    private static final String ADMIN_SETUP_LOCK = "smart_campus_admin_bootstrap";
    private static final int BCRYPT_COST = 12;
    private static final int MAX_BCRYPT_PASSWORD_BYTES = 72;
    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{2,79}");
    private static final Pattern GMAIL_PATTERN =
            Pattern.compile("[A-Za-z0-9](?:[A-Za-z0-9._%+-]{0,62}[A-Za-z0-9])?@gmail\\.com",
                    Pattern.CASE_INSENSITIVE);

    public AuthenticatedUser login(String username, String gmailAddress, char[] password) {
        if (username == null || username.trim().isEmpty()
                || !isValidGmailAddress(gmailAddress)
                || password == null || password.length == 0
                || passwordByteLength(password) > MAX_BCRYPT_PASSWORD_BYTES) {
            return null;
        }

        String sql = "SELECT id, username, email, password_hash, role, enabled "
                + "FROM users "
                + "WHERE (LOWER(username) = LOWER(?) "
                + "OR (username IS NULL AND LOWER(SUBSTRING_INDEX(email, '@', 1)) = LOWER(?))) "
                + "AND LOWER(email) = LOWER(?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username.trim());
            statement.setString(2, username.trim());
            statement.setString(3, gmailAddress.trim());

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }

                String storedHash = result.getString("password_hash");
                boolean passwordMatches;
                try {
                    passwordMatches = storedHash != null
                            && BCrypt.checkpw(new String(password), storedHash);
                } catch (IllegalArgumentException exception) {
                    passwordMatches = false;
                }

                if (!passwordMatches || !result.getBoolean("enabled")
                        || !isAdministratorRole(result.getString("role"))) {
                    return null;
                }

                String storedUsername = result.getString("username");
                if (storedUsername == null || storedUsername.trim().isEmpty()) {
                    storedUsername = username.trim();
                }

                return new AuthenticatedUser(
                        result.getLong("id"),
                        storedUsername,
                        result.getString("email"),
                        UserRole.ADMINISTRATOR);
            }
        } catch (SQLException exception) {
            throw new DatabaseException("Administrator login failed because of a database error.", exception);
        }
    }

    public AuthenticatedUser login(String email, char[] password) {
        if (email == null || !isValidGmailAddress(email)
                || password == null || password.length == 0
                || passwordByteLength(password) > MAX_BCRYPT_PASSWORD_BYTES) {
            return null;
        }

        String sql = "SELECT id, username, email, password_hash, role, enabled "
                + "FROM users WHERE LOWER(email) = LOWER(?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email.trim());

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }

                String storedHash = result.getString("password_hash");
                boolean passwordMatches;
                try {
                    passwordMatches = storedHash != null
                            && BCrypt.checkpw(new String(password), storedHash);
                } catch (IllegalArgumentException exception) {
                    passwordMatches = false;
                }

                if (!passwordMatches || !result.getBoolean("enabled")) {
                    return null;
                }

                UserRole role = mapRole(result.getString("role"));
                if (role == null) {
                    return null;
                }

                String storedUsername = result.getString("username");
                if (storedUsername == null || storedUsername.trim().isEmpty()) {
                    storedUsername = extractLocalPart(email.trim());
                }

                return new AuthenticatedUser(
                        result.getLong("id"),
                        storedUsername,
                        result.getString("email"),
                        role);
            }
        } catch (SQLException exception) {
            throw new DatabaseException("Login failed because of a database error.", exception);
        }
    }

    public static UserRole mapRole(String role) {
        if (role == null) {
            return null;
        }
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        if (normalized.equals("ADMIN") || normalized.equals("ADMINISTRATOR")) {
            return UserRole.ADMINISTRATOR;
        }
        if (normalized.equals("FACULTY")) {
            return UserRole.FACULTY;
        }
        if (normalized.equals("STUDENT")) {
            return UserRole.STUDENT;
        }
        return null;
    }

    public boolean isFirstAdministratorSetupAvailable() {
        String sql = "SELECT email FROM users "
                + "WHERE enabled = TRUE "
                + "AND UPPER(role) IN ('ADMIN', 'ADMINISTRATOR')";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                if (isValidGmailAddress(result.getString("email"))) {
                    return false;
                }
            }
            return true;
        } catch (SQLException exception) {
            throw new DatabaseException("Could not check administrator setup status.", exception);
        }
    }

    public boolean createFirstAdministrator(
            String username, String gmailAddress, char[] password) {
        validateNewAdministrator(username, gmailAddress, password);

        try (Connection connection = DatabaseConnection.getConnection()) {
            if (!acquireSetupLock(connection)) {
                throw new DatabaseException(
                        "Administrator setup is busy. Please try again.");
            }

            try {
                if (!isFirstAdministratorSetupAvailable(connection)) {
                    return false;
                }

                String sql = "INSERT INTO users "
                        + "(username, email, password_hash, role, enabled) "
                        + "VALUES (?, ?, ?, 'ADMIN', TRUE)";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, username.trim());
                    statement.setString(2, gmailAddress.trim().toLowerCase(Locale.ROOT));
                    statement.setString(3, hashPassword(password));
                    statement.executeUpdate();
                    return true;
                }
            } catch (SQLException exception) {
                if (exception.getErrorCode() == 1062) {
                    throw new DatabaseException(
                            "That administrator username or Gmail address is already registered.",
                            exception);
                }
                throw new DatabaseException("Could not create the administrator account.", exception);
            } finally {
                releaseSetupLock(connection);
            }
        } catch (SQLException exception) {
            throw new DatabaseException("Could not create the administrator account.", exception);
        }
    }

    public static boolean isValidGmailAddress(String gmailAddress) {
        if (gmailAddress == null) {
            return false;
        }
        String normalized = gmailAddress.trim();
        return normalized.length() <= 254
                && !normalized.contains("..")
                && GMAIL_PATTERN.matcher(normalized).matches();
    }

    public static boolean isAdministratorRole(String role) {
        if (role == null) {
            return false;
        }
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        return normalized.equals("ADMIN") || normalized.equals("ADMINISTRATOR");
    }

    static void validateNewAdministrator(
            String username, String gmailAddress, char[] password) {
        if (username == null || !USERNAME_PATTERN.matcher(username.trim()).matches()) {
            throw new IllegalArgumentException(
                    "Username must be 3-80 characters and use letters, numbers, dots, underscores, or hyphens.");
        }
        if (!isValidGmailAddress(gmailAddress)) {
            throw new IllegalArgumentException("Enter a valid @gmail.com address.");
        }
        if (password == null || password.length < 12
                || passwordByteLength(password) > MAX_BCRYPT_PASSWORD_BYTES) {
            throw new IllegalArgumentException(
                    "Password must be at least 12 characters and no more than 72 UTF-8 bytes.");
        }
    }

    static String hashPassword(char[] password) {
        return BCrypt.hashpw(new String(password), BCrypt.gensalt(BCRYPT_COST));
    }

    private static String extractLocalPart(String emailAddress) {
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            return "user";
        }
        String trimmed = emailAddress.trim();
        int atIndex = trimmed.indexOf('@');
        return atIndex > 0 ? trimmed.substring(0, atIndex) : trimmed;
    }

    private static int passwordByteLength(char[] password) {
        return new String(password).getBytes(StandardCharsets.UTF_8).length;
    }

    private boolean isFirstAdministratorSetupAvailable(Connection connection)
            throws SQLException {
        String sql = "SELECT email FROM users "
                + "WHERE enabled = TRUE "
                + "AND UPPER(role) IN ('ADMIN', 'ADMINISTRATOR')";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                if (isValidGmailAddress(result.getString("email"))) {
                    return false;
                }
            }
            return true;
        }
    }

    private boolean acquireSetupLock(Connection connection) throws SQLException {
        try (PreparedStatement statement =
                     connection.prepareStatement("SELECT GET_LOCK(?, 10)")) {
            statement.setString(1, ADMIN_SETUP_LOCK);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() && result.getInt(1) == 1;
            }
        }
    }

    private void releaseSetupLock(Connection connection) {
        try (PreparedStatement statement =
                     connection.prepareStatement("SELECT RELEASE_LOCK(?)")) {
            statement.setString(1, ADMIN_SETUP_LOCK);
            statement.executeQuery().close();
        } catch (SQLException exception) {
            throw new DatabaseException(
                    "Administrator setup completed, but its database lock could not be released.",
                    exception);
        }
    }
}
