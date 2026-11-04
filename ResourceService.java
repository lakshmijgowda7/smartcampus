import java.util.ArrayList;
import java.util.Scanner;

public class ResourceService {
    private final ArrayList<Resource> resources = new ArrayList<>();

    public static void main(String[] args) {
        runApplication(new Scanner(System.in));
    }

    public static void runApplication(Scanner scanner) {
        UserAuthService authService = new UserAuthService();
        ResourceService resourceService = new ResourceService();
        CampusActivityService activityService = new CampusActivityService();
        ResourceBookingService bookingService = new ResourceBookingService(resourceService);
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("=== Smart Campus Login ===");
            System.out.println("1. Register");
            System.out.println("2. Log In");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
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
                    System.out.println("Invalid choice. Enter a number from 1 to 3.");
            }
        }
    }

    private static void runUserMenu(Scanner scanner, UserAccount account,
                                    ResourceService resourceService,
                                    CampusActivityService activityService,
                                    ResourceBookingService bookingService,
                                    UserAuthService authService) {
        boolean administrator = account.getRole() == UserRole.ADMINISTRATOR;
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println();
            System.out.println("=== Smart Campus Management ===");
            System.out.println("Signed in as " + account.getUsername()
                    + " (" + account.getRole() + ")");
            if (administrator) {
                System.out.println("1. Resource Management");
                System.out.println("2. Campus Activity Management");
                System.out.println("3. Resource Booking");
                System.out.println("4. Create User Account");
                System.out.println("5. Log Out");
                System.out.print("Enter your choice: ");
                switch (scanner.nextLine().trim()) {
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
                        System.out.println("Invalid choice. Enter a number from 1 to 5.");
                }
            } else {
                System.out.println("1. Resource Booking");
                System.out.println("2. Log Out");
                System.out.print("Enter your choice: ");
                switch (scanner.nextLine().trim()) {
                    case "1":
                        bookingService.runMenu(scanner);
                        break;
                    case "2":
                        loggedIn = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Enter 1 or 2.");
                }
            }
        }
        System.out.println("Logged out successfully.");
    }

    private static void registerFromConsole(Scanner scanner, UserAuthService authService) {
        String username = readConsoleText(scanner, "Username: ");
        char[] password = readConsolePassword(scanner);
        UserRole role;
        if (authService.getAccountCount() == 0) {
            role = UserRole.ADMINISTRATOR;
            System.out.println("The first registered account will be the Administrator.");
        } else {
            role = readSelfRegistrationRole(scanner);
        }

        try {
            UserAccount account = authService.register(username, password, role);
            System.out.println("Account created successfully for "
                    + account.getUsername() + " (" + account.getRole() + ").");
        } catch (IllegalArgumentException exception) {
            System.out.println("Registration failed: " + exception.getMessage());
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    private static void createAccountFromConsole(Scanner scanner, UserAuthService authService) {
        String username = readConsoleText(scanner, "New username: ");
        char[] password = readConsolePassword(scanner);
        UserRole role = readAdministratorAssignedRole(scanner);

        try {
            authService.createAccount(username, password, role, UserRole.ADMINISTRATOR);
            System.out.println("User account created successfully.");
        } catch (IllegalArgumentException exception) {
            System.out.println("Account creation failed: " + exception.getMessage());
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    private static UserAccount loginFromConsole(Scanner scanner, UserAuthService authService) {
        String username = readConsoleText(scanner, "Username: ");
        char[] password = readConsolePassword(scanner);
        try {
            UserAccount account = authService.authenticate(username, password);
            if (account == null) {
                System.out.println("Login failed: invalid username or password.");
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
            System.out.println("Choose account role: 1. Student  2. Faculty");
            System.out.print("Enter your choice: ");
            switch (scanner.nextLine().trim()) {
                case "1":
                    return UserRole.STUDENT;
                case "2":
                    return UserRole.FACULTY;
                default:
                    System.out.println("Invalid choice. Enter 1 or 2.");
            }
        }
    }

    private static UserRole readAdministratorAssignedRole(Scanner scanner) {
        while (true) {
            System.out.println("Choose account role: 1. Administrator  2. Student  3. Faculty");
            System.out.print("Enter your choice: ");
            switch (scanner.nextLine().trim()) {
                case "1":
                    return UserRole.ADMINISTRATOR;
                case "2":
                    return UserRole.STUDENT;
                case "3":
                    return UserRole.FACULTY;
                default:
                    System.out.println("Invalid choice. Enter a number from 1 to 3.");
            }
        }
    }

    private static String readConsoleText(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Input cannot be empty. Please try again.");
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

    public void runMenu(Scanner scanner) {
        if (scanner == null) {
            throw new IllegalArgumentException("Scanner cannot be null.");
        }

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("=== Resource Management ===");
            System.out.println("1. Add Resource");
            System.out.println("2. View All Resources");
            System.out.println("3. Search Resource");
            System.out.println("4. Update Resource");
            System.out.println("5. Delete Resource");
            System.out.println("6. Check Resource Availability");
            System.out.println("7. View Available Resources");
            System.out.println("8. Back to Main Menu");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
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
                    System.out.println("Invalid choice. Enter a number from 1 to 8.");
            }
        }
    }

    public synchronized void addResource(Scanner scanner) {
        String resourceId = readRequiredText(scanner, "Resource ID: ");
        if (findResource(resourceId) != null) {
            System.out.println("Error: A resource with that ID already exists.");
            return;
        }

        String resourceName = readRequiredText(scanner, "Resource name: ");
        String resourceType = readResourceType(scanner);
        String buildingName = readRequiredText(scanner, "Building name: ");
        int capacity = readPositiveCapacity(scanner);
        String availabilityStatus = readAvailabilityStatus(scanner);

        synchronized (resources) {
            if (findResource(resourceId) != null) {
                System.out.println("Error: A resource with that ID already exists.");
                return;
            }
            resources.add(new Resource(resourceId, resourceName, resourceType,
                    buildingName, capacity, availabilityStatus));
        }
        System.out.println("Resource added successfully.");
    }

    public synchronized void viewAllResources() {
        if (resources.isEmpty()) {
            System.out.println("No resources have been added.");
            return;
        }

        for (Resource resource : resources) {
            resource.displayResource();
        }
    }

    public void searchResource(Scanner scanner) {
        Resource resource = findResource(readRequiredText(scanner, "Resource ID to search: "));
        if (resource == null) {
            System.out.println("Error: Resource not found.");
            return;
        }
        resource.displayResource();
    }

    public synchronized void updateResource(Scanner scanner) {
        Resource resource = findResource(readRequiredText(scanner, "Resource ID to update: "));
        if (resource == null) {
            System.out.println("Error: Resource not found. Nothing was updated.");
            return;
        }

        resource.setResourceName(readRequiredText(scanner, "New resource name: "));
        resource.setResourceType(readResourceType(scanner));
        resource.setBuildingName(readRequiredText(scanner, "New building name: "));
        resource.setCapacity(readPositiveCapacity(scanner));
        resource.setAvailabilityStatus(readAvailabilityStatus(scanner));
        System.out.println("Resource updated successfully.");
    }

    public synchronized void deleteResource(Scanner scanner) {
        Resource resource = findResource(readRequiredText(scanner, "Resource ID to delete: "));
        if (resource == null) {
            System.out.println("Error: Resource not found. Nothing was deleted.");
            return;
        }

        resources.remove(resource);
        System.out.println("Resource deleted successfully.");
    }

    public void checkResourceAvailability(Scanner scanner) {
        Resource resource = findResource(readRequiredText(scanner, "Resource ID to check: "));
        if (resource == null) {
            System.out.println("Error: Resource not found.");
            return;
        }
        System.out.println("Resource " + resource.getResourceId() + " is "
                + resource.getAvailabilityStatus() + ".");
    }

    public synchronized void viewAvailableResources() {
        boolean foundAvailable = false;
        for (Resource resource : resources) {
            if (resource.isAvailable()) {
                resource.displayResource();
                foundAvailable = true;
            }
        }

        if (!foundAvailable) {
            System.out.println("No available resources were found.");
        }
    }

    public synchronized Resource findResourceById(String resourceId) {
        if (resourceId == null) {
            return null;
        }
        synchronized (resources) {
            for (Resource resource : resources) {
                if (resource.getResourceId().equalsIgnoreCase(resourceId.trim())) {
                    return resource;
                }
            }
        }
        return null;
    }

    private Resource findResource(String resourceId) {
        return findResourceById(resourceId);
    }

    private String readRequiredText(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Input cannot be empty. Please try again.");
        }
    }

    private String readResourceType(Scanner scanner) {
        while (true) {
            System.out.print("Resource type (Classroom, Laboratory, Equipment): ");
            String value = scanner.nextLine().trim();
            try {
                return Resource.ResourceType.parse(value).displayName;
            } catch (IllegalArgumentException exception) {
                System.out.println(exception.getMessage());
            }
        }
    }

    private int readPositiveCapacity(Scanner scanner) {
        while (true) {
            System.out.print("Capacity (positive whole number): ");
            String value = scanner.nextLine().trim();
            try {
                int capacity = Integer.parseInt(value);
                if (capacity > 0) {
                    return capacity;
                }
            } catch (NumberFormatException exception) {
                System.out.println("Capacity must be a valid whole number.");
                continue;
            }
            System.out.println("Capacity must be greater than zero.");
        }
    }

    private String readAvailabilityStatus(Scanner scanner) {
        while (true) {
            System.out.print("Availability (Available or Unavailable): ");
            String value = scanner.nextLine().trim();
            if (value.equalsIgnoreCase("Available")) {
                return "Available";
            }
            if (value.equalsIgnoreCase("Unavailable")) {
                return "Unavailable";
            }
            System.out.println("Status must be Available or Unavailable.");
        }
    }
}
