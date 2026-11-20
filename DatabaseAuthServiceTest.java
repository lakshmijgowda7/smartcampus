import java.util.Arrays;

import org.mindrot.jbcrypt.BCrypt;

public final class DatabaseAuthServiceTest {
    public static void main(String[] args) {
        assertTrue(DatabaseAuthService.isValidGmailAddress("admin@gmail.com"));
        assertTrue(DatabaseAuthService.isValidGmailAddress("ADMIN.Name+campus@gmail.com"));
        assertFalse(DatabaseAuthService.isValidGmailAddress("admin@campus.edu"));
        assertFalse(DatabaseAuthService.isValidGmailAddress("admin@gmail.com.example.org"));
        assertFalse(DatabaseAuthService.isValidGmailAddress("admin..name@gmail.com"));
        assertFalse(DatabaseAuthService.isValidGmailAddress(null));

        assertTrue(DatabaseAuthService.isAdministratorRole("ADMIN"));
        assertTrue(DatabaseAuthService.isAdministratorRole(" administrator "));
        assertFalse(DatabaseAuthService.isAdministratorRole("FACULTY"));
        assertFalse(DatabaseAuthService.isAdministratorRole("STUDENT"));
        assertFalse(DatabaseAuthService.isAdministratorRole(null));

        AuthenticatedUser administrator = new AuthenticatedUser(
                1L, "admin", "admin@gmail.com", UserRole.ADMINISTRATOR);
        AuthenticatedUser faculty = new AuthenticatedUser(
                2L, "faculty", "faculty@gmail.com", UserRole.FACULTY);
        AuthenticatedUser student = new AuthenticatedUser(
                3L, "student", "student@gmail.com", UserRole.STUDENT);
        assertTrue(Main.canAccessAdministratorFeatures(administrator));
        assertFalse(Main.canAccessAdministratorFeatures(faculty));
        assertFalse(Main.canAccessAdministratorFeatures(student));
        assertFalse(Main.canAccessAdministratorFeatures(null));

        expectInvalid(() -> DatabaseAuthService.validateNewAdministrator(
                "admin", "admin@gmail.com", "shortPass1".toCharArray()));
        expectInvalid(() -> DatabaseAuthService.validateNewAdministrator(
                "admin user", "admin@gmail.com", "a-very-long-password".toCharArray()));
        expectInvalid(() -> DatabaseAuthService.validateNewAdministrator(
                "admin", "admin@gmail.com", new char[73]));

        char[] password = "a-secure-password".toCharArray();
        try {
            String passwordHash = DatabaseAuthService.hashPassword(password);
            assertFalse(passwordHash.equals(new String(password)));
            assertTrue(BCrypt.checkpw(new String(password), passwordHash));
        } finally {
            Arrays.fill(password, '\0');
        }

        System.out.println("Database authentication validation tests passed.");
    }

    private static void expectInvalid(Runnable action) {
        try {
            action.run();
            throw new AssertionError("Expected invalid administrator input to be rejected.");
        } catch (IllegalArgumentException expected) {
            // Expected validation failure.
        }
    }

    private static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected condition to be true.");
        }
    }

    private static void assertFalse(boolean condition) {
        assertTrue(!condition);
    }
}
