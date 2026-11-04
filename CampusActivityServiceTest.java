import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class CampusActivityServiceTest {
    public static void main(String[] args) {
        testActivityOperations();
        testCombinedApplicationMenu();
        expectInvalid(() -> new CampusEvent("EVT1", "", "2026-11-10", "Quad", "Fair"));
        System.out.println("Campus activity tests passed.");
    }

    private static void testActivityOperations() {
        String input = String.join("\n",
                "1", "EVT1", "Campus Fair", "2026-11-10", "Main Quad", "Annual campus fair",
                "1", "EVT2", "Science Talk", "2026-12-01", "Lecture Hall", "Guest lecture",
                "1", "EVT1",
                "2",
                "3", "EVT2",
                "3", "MISSING",
                "4", "EVT1", "Updated Fair", "2026-11-11", "East Quad", "Updated details",
                "4", "MISSING",
                "5", "MISSING",
                "5", "EVT2",
                "2",
                "9",
                "6", "");

        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try {
            System.setOut(new PrintStream(captured));
            new CampusActivityService().runMenu(new Scanner(input));
        } finally {
            System.setOut(originalOutput);
        }

        String output = captured.toString();
        String[] expected = {
                "Event added successfully.",
                "An event with that ID already exists.",
                "Event ID: EVT1 | Name: Campus Fair",
                "Event ID: EVT2 | Name: Science Talk",
                "Event updated successfully.",
                "Event ID: EVT1 | Name: Updated Fair | Date: 2026-11-11",
                "Event not found.",
                "Nothing was updated.",
                "Nothing was deleted.",
                "Event deleted successfully.",
                "Enter a number from 1 to 6."
        };
        for (String expectedText : expected) {
            if (!output.contains(expectedText)) {
                throw new AssertionError("Missing expected console output: " + expectedText);
            }
        }
        if (countOccurrences(output, "Event ID: EVT2") != 2) {
            throw new AssertionError("Deleted event was missing before deletion or remained afterward.");
        }
    }

    private static void testCombinedApplicationMenu() {
        InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        String input = String.join("\n",
                "1", "admin", "AdminPass123",
                "2", "admin", "AdminPass123",
                "1", "8",
                "2", "6",
                "3", "4",
                "4", "student", "StudentPass123", "2",
                "5",
                "2", "student", "StudentPass123",
                "1", "4",
                "2",
                "3", "");
        try {
            System.setIn(new java.io.ByteArrayInputStream(
                    input.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(captured));
            ResourceService.runApplication(new java.util.Scanner(System.in));
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
        }

        String output = captured.toString();
        if (!output.contains("Login successful.")
                || !output.contains("=== Campus Activity Management ===")
                || !output.contains("=== Resource Booking ===")
                || !output.contains("=== Resource Management ===")
                || !output.contains("Signed in as admin (ADMINISTRATOR)")
                || !output.contains("Signed in as student (STUDENT)")) {
            throw new AssertionError("The combined menu did not open both features and return.");
        }
        int studentMenu = output.indexOf("Signed in as student (STUDENT)");
        int studentLogout = output.indexOf("Logged out successfully.", studentMenu);
        String studentOptions = output.substring(studentMenu, studentLogout);
        if (studentOptions.contains("Resource Management")
                || studentOptions.contains("Campus Activity Management")
                || studentOptions.contains("Create User Account")) {
            throw new AssertionError("Student account was shown administrator-only menu options.");
        }
    }

    private static int countOccurrences(String text, String value) {
        int count = 0;
        int offset = 0;
        while ((offset = text.indexOf(value, offset)) >= 0) {
            count++;
            offset += value.length();
        }
        return count;
    }

    private static void expectInvalid(Runnable action) {
        try {
            action.run();
            throw new AssertionError("Expected invalid event data to be rejected.");
        } catch (IllegalArgumentException expected) {
            // Expected validation failure.
        }
    }
}
