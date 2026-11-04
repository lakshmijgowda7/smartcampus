import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class ResourceServiceTest {
    public static void main(String[] args) {
        String input = String.join("\n",
                "1", "C101", "Lecture Hall", "Classroom", "North", "30", "Available",
                "1", "L201", "Physics Lab", "Laboratory", "Science", "20", "Unavailable",
                "1", "E301", "Projector", "Equipment", "Main", "1", "Available",
                "1", "C101",
                "1", "X401", "Camera", "Invalid", "Equipment", "Media", "zero", "0", "2",
                "sometimes", "Available",
                "2",
                "3", "L201",
                "4", "E301", "HD Projector", "Equipment", "Main", "2", "Available",
                "6", "E301",
                "7",
                "3", "MISSING",
                "4", "MISSING",
                "5", "MISSING",
                "5", "X401",
                "2",
                "9",
                "8", "");

        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try {
            System.setOut(new PrintStream(captured));
            new ResourceService().runMenu(new Scanner(input));
        } finally {
            System.setOut(originalOutput);
        }

        String output = captured.toString();
        String[] expected = {
                "Resource added successfully.",
                "A resource with that ID already exists.",
                "Resource type must be Classroom, Laboratory, or Equipment.",
                "Capacity must be a valid whole number.",
                "Capacity must be greater than zero.",
                "Status must be Available or Unavailable.",
                "Resource ID: C101",
                "Resource ID: L201",
                "Resource ID: E301",
                "Resource updated successfully.",
                "Resource E301 is Available.",
                "Resource not found.",
                "Nothing was updated.",
                "Nothing was deleted.",
                "Resource deleted successfully.",
                "Enter a number from 1 to 8."
        };
        for (String expectedText : expected) {
            if (!output.contains(expectedText)) {
                throw new AssertionError("Missing expected console output: " + expectedText);
            }
        }
        String[] menuItems = {
                "1. Add Resource",
                "2. View All Resources",
                "3. Search Resource",
                "4. Update Resource",
                "5. Delete Resource",
                "6. Check Resource Availability",
                "7. View Available Resources",
                "8. Back to Main Menu"
        };
        for (String menuItem : menuItems) {
            if (!output.contains(menuItem)) {
                throw new AssertionError("Missing submenu item: " + menuItem);
            }
        }
        int availabilityCheck = output.indexOf("Resource E301 is Available.");
        int availableStart = output.indexOf("Resource ID: C101", availabilityCheck);
        int nextMenu = output.indexOf("=== Resource Management ===", availableStart);
        if (availabilityCheck < 0 || availableStart < 0 || nextMenu < 0
                || output.substring(availableStart, nextMenu).contains("Resource ID: L201")) {
            throw new AssertionError("Unavailable resource appeared in the available resources list.");
        }
        if (countOccurrences(output, "Resource ID: X401") != 2) {
            throw new AssertionError("Deleted resource was missing before deletion or remained afterward.");
        }

        Resource resource = new Resource("T1", "Test", "Classroom", "North", 1, "Available");
        expectInvalid(() -> resource.setResourceType("Office"));
        expectInvalid(() -> resource.setCapacity(0));
        expectInvalid(() -> resource.setAvailabilityStatus("Maybe"));

        System.out.println("ResourceService tests passed.");
    }

    private static void expectInvalid(Runnable action) {
        try {
            action.run();
            throw new AssertionError("Expected invalid value to be rejected.");
        } catch (IllegalArgumentException expected) {
            // Expected validation failure.
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
}
