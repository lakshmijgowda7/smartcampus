public class UserAuthServiceTest {
    public static void main(String[] args) {
        UserAuthService authService = new UserAuthService();

        UserAccount administrator = authService.register(
                "admin", "AdminPass123".toCharArray(), UserRole.ADMINISTRATOR);
        if (administrator.getRole() != UserRole.ADMINISTRATOR) {
            throw new AssertionError("The initial account was not created as Administrator.");
        }

        expectInvalid(() -> authService.register(
                "another-admin", "AdminPass123".toCharArray(), UserRole.ADMINISTRATOR));
        expectInvalid(() -> authService.register(
                "weak", "short".toCharArray(), UserRole.STUDENT));
        expectInvalid(() -> authService.register(
                " ADMIN ", "DifferentPass123".toCharArray(), UserRole.STUDENT));
        expectInvalid(() -> authService.createAccount(
                "unauthorized", "ValidPass123".toCharArray(),
                UserRole.STUDENT, UserRole.STUDENT));

        UserAccount student = authService.register(
                "student", "StudentPass123".toCharArray(), UserRole.STUDENT);
        UserAccount faculty = authService.createAccount(
                "faculty", "FacultyPass123".toCharArray(),
                UserRole.FACULTY, administrator.getRole());
        UserAccount anotherStudent = authService.register(
                "student2", "StudentPass123".toCharArray(), UserRole.STUDENT);

        if (student.getRole() != UserRole.STUDENT
                || faculty.getRole() != UserRole.FACULTY
                || authService.getAccountCount() != 4) {
            throw new AssertionError("Account roles or account count were incorrect.");
        }
        if (authService.authenticate("student", "wrong-password".toCharArray()) != null) {
            throw new AssertionError("Incorrect password was accepted.");
        }
        if (authService.authenticate("missing", "StudentPass123".toCharArray()) != null) {
            throw new AssertionError("Unknown username was authenticated.");
        }
        if (authService.authenticate("STUDENT", "StudentPass123".toCharArray()) != student) {
            throw new AssertionError("Valid case-insensitive username/password did not authenticate.");
        }
        if (authService.authenticate("student2", "StudentPass123".toCharArray()) != anotherStudent) {
            throw new AssertionError("Same password did not authenticate for its account.");
        }

        System.out.println("User authentication tests passed.");
    }

    private static void expectInvalid(Runnable action) {
        try {
            action.run();
            throw new AssertionError("Expected account validation to reject this request.");
        } catch (IllegalArgumentException expected) {
            // Expected validation failure.
        }
    }
}
