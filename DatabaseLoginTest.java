import java.io.Console;
import java.util.Arrays;

public final class DatabaseLoginTest {
    public static void main(String[] args) {
        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException(
                    "Run this login check from an interactive terminal so the password is not echoed.");
        }

        String username = console.readLine("Administrator username: ");
        String gmailAddress = console.readLine("Administrator Gmail address: ");
        char[] password = console.readPassword("Password: ");
        if (password == null) {
            System.out.println("Login cancelled.");
            return;
        }

        try {
            AuthenticatedUser user =
                    new DatabaseAuthService().login(username, gmailAddress, password);
            if (user == null) {
                System.out.println("Invalid administrator credentials or role.");
                return;
            }

            System.out.println("Administrator login successful. Welcome, "
                    + user.getUsername() + ".");
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}
