
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class ResourceService {

    public static void main(String[] args) {
        Main.main(args);
    }

    // Existing standalone application entry point retained.
    public static void runApplication(Scanner scanner) {
        UserAuthService authService = new UserAuthService();
        ResourceService resourceService = new ResourceService();
        CampusActivityService activityService = new CampusActivityService();
        ResourceBookingService bookingService =
                new ResourceBookingService(resourceService);

        boolean running = true;

        while (running) {
            System.out.println("\n=== Smart Campus Login ===");
            System.out.println("1. Register");
            System.out.println("2. Log In");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            switch (scanner.nextLine().trim()) {
                case "1":
                    registerFromConsole(scanner, authService);
                    break;
                case "2":
                    UserAccount account = loginFromConsole(scanner, authService);
                    if (account != null) {
                        runUserMenu(scanner, account, resourceService,
                                activityService, bookingService, authService);
                    }
                    break;
                case "3":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Enter 1 to 3.");
            }
        }
    }

    private static void runUserMenu(
            Scanner scanner,
            UserAccount account,
            ResourceService resourceService,
            CampusActivityService activityService,
            ResourceBookingService bookingService,
            UserAuthService authService) {

        boolean administrator =
                account.getRole() == UserRole.ADMINISTRATOR;

        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n=== Smart Campus Management ===");
            System.out.println("Signed in as " + account.getUsername()
                    + " (" + account.getRole() + ")");

            if (administrator) {
                System.out.println("1. Resource Management");
                System.out.println("2. Campus Activity Management");
                System.out.println("3. Resource Booking");
                System.out.println("4. Create User Account");
                System.out.println("5. Log Out");
            } else {
                System.out.println("1. Resource Booking");
                System.out.println("2. Log Out");
            }

            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine().trim();

            if (administrator) {
                switch (choice) {
                    case "1":
                        resourceService.runMenu(scanner);
                        break;
                    case "2":
                        activityService.runMenu(scanner);
                        break;
                    case "3":
                        bookingService.runMenu(scanner);
                        break;
                    case "4":
                        createAccountFromConsole(scanner, authService);
                        break;
                    case "5":
                        loggedIn = false;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } else {
                switch (choice) {
                    case "1":
                        bookingService.runMenu(scanner);
                        break;
                    case "2":
                        loggedIn = false;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            }
        }

        System.out.println("Logged out successfully.");
    }

    private static void registerFromConsole(
            Scanner scanner, UserAuthService authService) {

        String username = readConsoleText(scanner, "Username: ");
        char[] password = readConsolePassword(scanner);

        UserRole role;

        if (authService.getAccountCount() == 0) {
            role = UserRole.ADMINISTRATOR;
            System.out.println(
                    "The first registered account will be Administrator.");
        } else {
            role = readSelfRegistrationRole(scanner);
        }

        try {
            UserAccount account =
                    authService.register(username, password, role);

            System.out.println("Account created successfully for "
                    + account.getUsername());
        } catch (IllegalArgumentException exception) {
            System.out.println("Registration failed: "
                    + exception.getMessage());
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    private static void createAccountFromConsole(
            Scanner scanner, UserAuthService authService) {

        String username = readConsoleText(scanner, "New username: ");
        char[] password = readConsolePassword(scanner);
        UserRole role = readAdministratorAssignedRole(scanner);

        try {
            authService.createAccount(
                    username, password, role, UserRole.ADMINISTRATOR);

            System.out.println("User account created successfully.");
        } catch (IllegalArgumentException exception) {
            System.out.println("Account creation failed: "
                    + exception.getMessage());
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    private static UserAccount loginFromConsole(
            Scanner scanner, UserAuthService authService) {

        String username = readConsoleText(scanner, "Username: ");
        char[] password = readConsolePassword(scanner);

        try {
            UserAccount account =
                    authService.authenticate(username, password);

            if (account == null) {
                System.out.println(
                        "Login failed: invalid username or password.");
                return null;
            }

            System.out.println("Login successful.");
            return account;
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    private static UserRole readSelfRegistrationRole(Scanner scanner) {
        while (true) {
            System.out.println("1. Student");
            System.out.println("2. Faculty");
            System.out.print("Choose role: ");

            switch (scanner.nextLine().trim()) {
                case "1":
                    return UserRole.STUDENT;
                case "2":
                    return UserRole.FACULTY;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static UserRole readAdministratorAssignedRole(
            Scanner scanner) {

        while (true) {
            System.out.println("1. Administrator");
            System.out.println("2. Student");
            System.out.println("3. Faculty");
            System.out.print("Choose role: ");

            switch (scanner.nextLine().trim()) {
                case "1":
                    return UserRole.ADMINISTRATOR;
                case "2":
                    return UserRole.STUDENT;
                case "3":
                    return UserRole.FACULTY;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static String readConsoleText(
            Scanner scanner, String prompt) {

        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();

            if (!value.isEmpty()) {
                return value;
            }

            System.out.println("Input cannot be empty.");
        }
    }

    private static char[] readConsolePassword(Scanner scanner) {
        while (true) {
            System.out.print("Password (at least 8 characters): ");
            char[] password = scanner.nextLine().toCharArray();

            if (password.length >= 8) {
                return password;
            }

            java.util.Arrays.fill(password, '\0');
            System.out.println("Password must contain at least 8 characters.");
        }
    }

    // ================= RESOURCE MANAGEMENT =================

    public void runMenu(Scanner scanner) {
        if (scanner == null) {
            throw new IllegalArgumentException("Scanner cannot be null.");
        }

        boolean running = true;

        while (running) {
            System.out.println("\n=== Resource Management ===");
            System.out.println("1. Add Resource");
            System.out.println("2. View All Resources");
            System.out.println("3. Search Resource");
            System.out.println("4. Update Resource");
            System.out.println("5. Delete Resource");
            System.out.println("6. Check Resource Availability");
            System.out.println("7. View Available Resources");
            System.out.println("8. Back to Main Menu");
            System.out.print("Enter your choice: ");

            switch (scanner.nextLine().trim()) {
                case "1":
                    addResource(scanner);
                    break;
                case "2":
                    viewAllResources();
                    break;
                case "3":
                    searchResource(scanner);
                    break;
                case "4":
                    updateResource(scanner);
                    break;
                case "5":
                    deleteResource(scanner);
                    break;
                case "6":
                    checkResourceAvailability(scanner);
                    break;
                case "7":
                    viewAvailableResources();
                    break;
                case "8":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Enter 1 to 8.");
            }
        }
    }

    public synchronized void addResource(Scanner scanner) {
        String resourceId =
                readRequiredText(scanner, "Resource ID: ");

        if (findResourceById(resourceId) != null) {
            System.out.println("Error: Resource ID already exists.");
            return;
        }

        String name = readRequiredText(scanner, "Resource name: ");
        String type = readResourceType(scanner);
        String building = readRequiredText(scanner, "Building name: ");
        int floor = readNonNegativeNumber(scanner, "Floor number: ");
        String room = readOptionalText(scanner, "Room number: ");
        int capacity = readPositiveCapacity(scanner);
        String status = readAvailabilityStatus(scanner);
        String notes = readOptionalText(scanner, "Maintenance notes: ");

        String sql = "INSERT INTO resources "
                + "(resource_code, name, resource_type, building, "
                + "floor_number, room_number, capacity, "
                + "operational_status, maintenance_notes) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, resourceId);
            statement.setString(2, name);
            statement.setString(3, toDatabaseType(type));
            statement.setString(4, building);
            statement.setInt(5, floor);
            statement.setString(6, room.isEmpty() ? null : room);
            statement.setInt(7, capacity);
            statement.setString(8, toDatabaseStatus(status));
            statement.setString(9, notes.isEmpty() ? null : notes);

            statement.executeUpdate();
            System.out.println("Resource added successfully.");

        } catch (SQLException exception) {
            System.out.println("Could not add resource: "
                    + exception.getMessage());
        }
    }

    public synchronized void viewAllResources() {
        String sql = "SELECT * FROM resources ORDER BY id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            boolean found = false;

            while (result.next()) {
                displayDatabaseResource(result);
                found = true;
            }

            if (!found) {
                System.out.println("No resources have been added.");
            }

        } catch (SQLException exception) {
            System.out.println("Could not retrieve resources: "
                    + exception.getMessage());
        }
    }

    public void searchResource(Scanner scanner) {
        String id = readRequiredText(scanner, "Resource ID to search: ");
        Resource resource = findResourceById(id);

        if (resource == null) {
            System.out.println("Error: Resource not found.");
        } else {
            resource.displayResource();
        }
    }

    public synchronized void updateResource(Scanner scanner) {
        String id = readRequiredText(scanner, "Resource ID to update: ");

        if (findResourceById(id) == null) {
            System.out.println("Error: Resource not found.");
            return;
        }

        String name = readRequiredText(scanner, "New resource name: ");
        String type = readResourceType(scanner);
        String building = readRequiredText(scanner, "New building name: ");
        int floor = readNonNegativeNumber(scanner, "New floor number: ");
        String room = readOptionalText(scanner, "New room number: ");
        int capacity = readPositiveCapacity(scanner);
        String status = readAvailabilityStatus(scanner);
        String notes = readOptionalText(scanner, "New maintenance notes: ");

        String sql = "UPDATE resources SET name=?, resource_type=?, "
                + "building=?, floor_number=?, room_number=?, capacity=?, "
                + "operational_status=?, maintenance_notes=? "
                + "WHERE resource_code=?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setString(2, toDatabaseType(type));
            statement.setString(3, building);
            statement.setInt(4, floor);
            statement.setString(5, room.isEmpty() ? null : room);
            statement.setInt(6, capacity);
            statement.setString(7, toDatabaseStatus(status));
            statement.setString(8, notes.isEmpty() ? null : notes);
            statement.setString(9, id);

            int updated = statement.executeUpdate();

            if (updated > 0) {
                System.out.println("Resource updated successfully.");
            } else {
                System.out.println("Resource not found.");
            }

        } catch (SQLException exception) {
            System.out.println("Could not update resource: "
                    + exception.getMessage());
        }
    }

    public synchronized void deleteResource(Scanner scanner) {
        String id = readRequiredText(scanner, "Resource ID to delete: ");

        String sql =
                "DELETE FROM resources WHERE resource_code=?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, id);
            int deleted = statement.executeUpdate();

            if (deleted > 0) {
                System.out.println("Resource deleted successfully.");
            } else {
                System.out.println("Error: Resource not found.");
            }

        } catch (SQLException exception) {
            System.out.println(
                    "Could not delete resource. It may have existing bookings. "
                    + exception.getMessage());
        }
    }

    public void checkResourceAvailability(Scanner scanner) {
        String id = readRequiredText(scanner, "Resource ID to check: ");
        Resource resource = findResourceById(id);

        if (resource == null) {
            System.out.println("Error: Resource not found.");
            return;
        }

        System.out.println("Resource " + resource.getResourceId()
                + " is " + resource.getAvailabilityStatus() + ".");
    }

    public synchronized void viewAvailableResources() {
        String sql = "SELECT * FROM resources "
                + "WHERE operational_status='OPERATIONAL' ORDER BY id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            boolean found = false;

            while (result.next()) {
                displayDatabaseResource(result);
                found = true;
            }

            if (!found) {
                System.out.println("No available resources were found.");
            }

        } catch (SQLException exception) {
            System.out.println("Could not retrieve available resources: "
                    + exception.getMessage());
        }
    }

    // Used by the existing ResourceBookingService.
    public synchronized Resource findResourceById(String resourceId) {
        if (resourceId == null || resourceId.trim().isEmpty()) {
            return null;
        }

        String sql =
                "SELECT * FROM resources WHERE resource_code=?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, resourceId.trim());

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return mapResource(result);
                }
            }

        } catch (SQLException exception) {
            System.out.println("Could not search resource: "
                    + exception.getMessage());
        }

        return null;
    }

    private Resource mapResource(ResultSet result)
            throws SQLException {

        String status = result.getString("operational_status");

        return new Resource(
                result.getString("resource_code"),
                result.getString("name"),
                fromDatabaseType(result.getString("resource_type")),
                result.getString("building"),
                result.getInt("floor_number"),
                result.getString("room_number"),
                result.getInt("capacity"),
                "OPERATIONAL".equalsIgnoreCase(status)
                        ? "Available" : "Unavailable",
                result.getString("maintenance_notes")
        );
    }

    private void displayDatabaseResource(ResultSet result)
            throws SQLException {
        mapResource(result).displayResource();
    }

    private String toDatabaseType(String type) {
        return Resource.ResourceType.parse(type).name();
    }

    private String fromDatabaseType(String type) {
        return Resource.ResourceType.parse(type).getDisplayName();
    }

    private String toDatabaseStatus(String status) {
        return "Available".equalsIgnoreCase(status)
                ? "OPERATIONAL" : "MAINTENANCE";
    }

    // ================= INPUT HELPERS =================

    private String readRequiredText(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();

            if (!value.isEmpty()) {
                return value;
            }

            System.out.println("Input cannot be empty.");
        }
    }

    private String readOptionalText(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private String readResourceType(Scanner scanner) {
        while (true) {
            System.out.print(
                    "Resource type (Classroom, Laboratory, Equipment, "
                    + "Seminar Hall): ");

            String value = scanner.nextLine().trim();

            try {
                return Resource.ResourceType.parse(value).getDisplayName();
            } catch (IllegalArgumentException exception) {
                System.out.println(exception.getMessage());
            }
        }
    }

    private int readPositiveCapacity(Scanner scanner) {
        while (true) {
            int value = readNumber(scanner,
                    "Capacity (positive whole number): ");

            if (value > 0) {
                return value;
            }

            System.out.println("Capacity must be greater than zero.");
        }
    }

    private int readNonNegativeNumber(Scanner scanner, String prompt) {
        while (true) {
            int value = readNumber(scanner, prompt);

            if (value >= 0) {
                return value;
            }

            System.out.println("Number cannot be negative.");
        }
    }

    private int readNumber(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);

            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException exception) {
                System.out.println("Enter a valid whole number.");
            }
        }
    }

    private String readAvailabilityStatus(Scanner scanner) {
        while (true) {
            System.out.print(
                    "Availability (Available or Unavailable): ");

            String value = scanner.nextLine().trim();

            if (value.equalsIgnoreCase("Available")) {
                return "Available";
            }

            if (value.equalsIgnoreCase("Unavailable")) {
                return "Unavailable";
            }

            System.out.println(
                    "Status must be Available or Unavailable.");
        }
    }
}
