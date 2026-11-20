
import java.io.Console;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.Arrays;
import java.util.function.Consumer;

public class Main {

    private static final DatabaseAuthService databaseAuthService = new DatabaseAuthService();
    private static final StudentService studentService = new StudentService();
    private static final FacultyService facultyService = new FacultyService();
    private static final CourseService courseService = new CourseService();
    private static final ResourceService resourceService = new ResourceService();
    private static final CampusActivityService campusActivityService = new CampusActivityService();
    private static final ResourceBookingService resourceBookingService =
            new ResourceBookingService(resourceService);

    public static void main(String[] args) {
        try {
            DatabaseConnection.testConnection();
            System.out.println("Connected to MySQL. Database storage is active; CSV files are not in use.");
        } catch (DatabaseException exception) {
            System.err.println("Cannot start Smart Campus: " + exception.getMessage());
            return;
        }

        Console console = System.console();
        Scanner scanner = new Scanner(System.in);

        boolean running = true;
        while (running) {
            boolean setupAvailable;
            try {
                setupAvailable = databaseAuthService.isFirstAdministratorSetupAvailable();
            } catch (DatabaseException exception) {
                System.err.println("Could not check administrator setup: " + exception.getMessage());
                return;
            }

            displayLoginMenu(setupAvailable);
            int maximumChoice = setupAvailable ? 3 : 2;
            int choice = (console != null)
                    ? readConsoleMenuChoice(console, 1, maximumChoice)
                    : readMenuChoice(scanner, 1, maximumChoice);

            switch (choice) {
                case 1:
                    AuthenticatedUser authenticatedUser = loginUser(console, scanner);
                    if (authenticatedUser != null) {
                        redirectToRoleDashboard(scanner, authenticatedUser);
                    }
                    break;
                case 2:
                    if (setupAvailable) {
                        createFirstAdministrator(console, scanner);
                    } else {
                        running = false;
                    }
                    break;
                case 3:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
        System.out.println("Exiting Smart Campus Management System. Goodbye!");
    }

    private static void displayLoginMenu(boolean setupAvailable) {
        System.out.println("=== SMART CAMPUS MANAGEMENT SYSTEM ===");
        System.out.println("1. Login");
        if (setupAvailable) {
            System.out.println("2. Create First Administrator");
            System.out.println("3. Exit");
        } else {
            System.out.println("2. Exit");
        }
        System.out.print("Enter your choice: ");
    }

    private static AuthenticatedUser loginUser(Console console, Scanner scanner) {
        String email = readPromptedText(console, scanner, "Email: ");
        char[] password = readPassword(console, scanner, "Password: ");
        if (password == null) {
            System.out.println("Login cancelled.");
            return null;
        }

        try {
            AuthenticatedUser user = databaseAuthService.login(email, password);
            if (user == null) {
                System.out.println("Login failed. Check the email and password.");
                return null;
            }

            System.out.println("Welcome, " + user.getUsername() + "!");
            return user;
        } catch (DatabaseException exception) {
            System.out.println("Login failed: " + exception.getMessage());
            return null;
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private static AuthenticatedUser administratorLogin(Console console, Scanner scanner) {
        String username = readPromptedText(console, scanner, "Admin username: ");
        String gmailAddress = readPromptedText(console, scanner, "Admin Gmail address: ");
        char[] password = readPassword(console, scanner, "Password: ");
        if (password == null) {
            System.out.println("Login cancelled.");
            return null;
        }

        try {
            AuthenticatedUser user =
                    databaseAuthService.login(username, gmailAddress, password);
            if (!canAccessAdministratorFeatures(user)) {
                System.out.println(
                        "Login failed. Check the username, Gmail address, password, and administrator role.");
                return null;
            }

            System.out.println("Welcome, " + user.getUsername() + "!");
            return user;
        } catch (DatabaseException exception) {
            System.out.println("Login failed: " + exception.getMessage());
            return null;
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private static void createFirstAdministrator(Console console, Scanner scanner) {
        String username = readPromptedText(console, scanner, "Admin username: ");
        String gmailAddress = readPromptedText(console, scanner, "Admin Gmail address: ");
        char[] password = readPassword(console, scanner, "Create password (at least 12 characters): ");
        if (password == null) {
            System.out.println("Administrator setup cancelled.");
            return;
        }
        char[] confirmation = readPassword(console, scanner, "Confirm password: ");
        if (confirmation == null) {
            Arrays.fill(password, '\0');
            System.out.println("Administrator setup cancelled.");
            return;
        }

        try {
            if (!Arrays.equals(password, confirmation)) {
                System.out.println("Administrator account was not created: passwords do not match.");
                return;
            }

            if (databaseAuthService.createFirstAdministrator(
                    username, gmailAddress, password)) {
                System.out.println("First administrator account created. You can now log in.");
            } else {
                System.out.println("An administrator account is already set up.");
            }
        } catch (IllegalArgumentException | DatabaseException exception) {
            System.out.println("Administrator setup failed: " + exception.getMessage());
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(confirmation, '\0');
        }
    }

    static boolean canAccessAdministratorFeatures(AuthenticatedUser user) {
        return user != null && user.getRole() == UserRole.ADMINISTRATOR;
    }

    private static void redirectToRoleDashboard(Scanner scanner, AuthenticatedUser user) {
        if (user == null) {
            return;
        }

        switch (user.getRole()) {
            case ADMINISTRATOR:
                runAdministratorDashboard(scanner, user);
                break;
            case FACULTY:
                runFacultyDashboard(scanner, user);
                break;
            case STUDENT:
                runStudentDashboard(scanner, user);
                break;
            default:
                System.out.println("Access denied. Unsupported role.");
        }
    }

    private static void runFacultyDashboard(Scanner scanner, AuthenticatedUser faculty) {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("=== FACULTY DASHBOARD ===");
            System.out.println("Welcome, " + faculty.getUsername() + "!");
            System.out.println("1. View Available Resources");
            System.out.println("2. Book Resource");
            System.out.println("3. View My Bookings");
            System.out.println("4. Logout");
            System.out.print("Enter your choice: ");

            int choice = readMenuChoice(scanner, 1, 4);
            switch (choice) {
                case 1:
                    resourceService.viewAvailableResources();
                    break;
                case 2:
                    bookResourceAsUser(scanner, faculty, UserRole.FACULTY);
                    break;
                case 3:
                    resourceBookingService.viewBookingsForUser(faculty.getId(), UserRole.FACULTY);
                    break;
                case 4:
                    running = false;
                    System.out.println("Logged out successfully.");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 1 to 4.");
            }
        }
    }

    private static void runStudentDashboard(Scanner scanner, AuthenticatedUser student) {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("=== STUDENT DASHBOARD ===");
            System.out.println("Welcome, " + student.getUsername() + "!");
            System.out.println("1. View Available Resources");
            System.out.println("2. Book Resource");
            System.out.println("3. View My Bookings");
            System.out.println("4. Logout");
            System.out.print("Enter your choice: ");

            int choice = readMenuChoice(scanner, 1, 4);
            switch (choice) {
                case 1:
                    resourceService.viewAvailableResources();
                    break;
                case 2:
                    bookResourceAsUser(scanner, student, UserRole.STUDENT);
                    break;
                case 3:
                    resourceBookingService.viewBookingsForUser(student.getId(), UserRole.STUDENT);
                    break;
                case 4:
                    running = false;
                    System.out.println("Logged out successfully.");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 1 to 4.");
            }
        }
    }

    private static void bookResourceAsUser(Scanner scanner, AuthenticatedUser user, UserRole role) {
        String resourceId = readRequiredText(scanner, "Resource ID: ");
        LocalDateTime startTime = readDateTime(scanner, "Start (yyyy-MM-dd HH:mm): ");
        LocalDateTime endTime = readDateTime(scanner, "End (yyyy-MM-dd HH:mm): ");
        resourceBookingService.setCurrentUserRole(role);
        resourceBookingService.setCurrentUserId(user.getId());
        resourceBookingService.bookResource(resourceId, startTime, endTime, role, user.getId());
    }

    private static void runAdministratorDashboard(
            Scanner scanner, AuthenticatedUser administrator) {
        if (!canAccessAdministratorFeatures(administrator)) {
            System.out.println("Access denied. Administrator privileges are required.");
            return;
        }

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("=== ADMINISTRATOR DASHBOARD ===");
            System.out.println("Welcome, " + administrator.getUsername() + "!");
            System.out.println("1. Student Management");
            System.out.println("2. Faculty Management");
            System.out.println("3. Course Management");
            System.out.println("4. Resource Management");
            System.out.println("5. Campus Activity Management");
            System.out.println("6. Resource Booking");
            System.out.println("7. Log Out");
            System.out.print("Enter your choice: ");

            int choice = readMenuChoice(scanner, 1, 7);
            switch (choice) {
                case 1:
                    studentManagementMenu(scanner);
                    break;
                case 2:
                    facultyManagementMenu(scanner);
                    break;
                case 3:
                    courseManagementMenu(scanner);
                    break;
                case 4:
                    resourceService.runMenu(scanner);
                    break;
                case 5:
                    campusActivityService.runMenu(scanner);
                    break;
                case 6:
                    resourceBookingService.setCurrentUserId(administrator.getId());
                    resourceBookingService.setCurrentUserRole(UserRole.ADMINISTRATOR);
                    resourceBookingService.runMenu(scanner);
                    break;
                case 7:
                    running = false;
                    System.out.println("Logged out successfully.");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 1 to 7.");
            }
        }
    }

    private static String readPromptedText(Console console, Scanner scanner, String prompt) {
        if (console != null) {
            return console.readLine(prompt);
        }
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private static char[] readPassword(Console console, Scanner scanner, String prompt) {
        if (console != null) {
            return console.readPassword(prompt);
        }
        System.out.print(prompt);
        String password = scanner.nextLine();
        return password.isEmpty() ? new char[0] : password.toCharArray();
    }

    private static int readConsoleMenuChoice(Console console, int minimum, int maximum) {
        while (true) {
            String input = console.readLine();
            if (input == null) {
                return maximum;
            }
            try {
                int choice = Integer.parseInt(input.trim());
                if (choice >= minimum && choice <= maximum) {
                    return choice;
                }
            } catch (NumberFormatException exception) {
                // Prompt again for non-numeric input.
            }
            console.printf("Please enter a valid number from %d to %d: ", minimum, maximum);
        }
    }

    private static int readMenuChoice(Scanner scanner, int minimum, int maximum) {
        while (true) {
            if (!scanner.hasNextLine()) {
                return maximum;
            }

            String input = scanner.nextLine().trim();

            try {
                int choice = Integer.parseInt(input);

                if (choice >= minimum && choice <= maximum) {
                    return choice;
                }
            } catch (NumberFormatException exception) {
                // Ask again below for non-numeric input.
            }

            System.out.print("Please enter a valid number from "
                    + minimum + " to " + maximum + ": ");
        }
    }

    private static LocalDateTime readDateTime(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            try {
                return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            } catch (DateTimeParseException exception) {
                System.out.println("Enter a valid date and time in yyyy-MM-dd HH:mm format.");
            }
        }
    }

    // STUDENT MANAGEMENT

    private static void studentManagementMenu(Scanner scanner) {
        boolean inStudentMenu = true;

        while (inStudentMenu) {
            displayStudentMenu();
            int choice = readMenuChoice(scanner, 1, 6);

            try {
                switch (choice) {
                    case 1:
                        addStudent(scanner);
                        break;
                    case 2:
                        viewAllStudents();
                        break;
                    case 3:
                        searchStudent(scanner);
                        break;
                    case 4:
                        updateStudent(scanner);
                        break;
                    case 5:
                        deleteStudent(scanner);
                        break;
                    case 6:
                        inStudentMenu = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number from 1 to 6.");
                }
            } catch (DatabaseException exception) {
                System.out.println("Database error: " + exception.getMessage());
            }

            System.out.println();
        }
    }

    private static void displayStudentMenu() {
        System.out.println("=== STUDENT MANAGEMENT ===");
        System.out.println("1. Add Student");
        System.out.println("2. View All Students");
        System.out.println("3. Search Student");
        System.out.println("4. Update Student");
        System.out.println("5. Delete Student");
        System.out.println("6. Return to Main Menu");
        System.out.print("Enter your choice: ");
    }

    private static void addStudent(Scanner scanner) {
        try {
            Student student = readStudentDetails(scanner);
            studentService.addStudent(student);
            System.out.println("Student added successfully.");
        } catch (IllegalArgumentException exception) {
            System.out.println("Error: " + exception.getMessage());
        }
    }

    private static void viewAllStudents() {
        displayRecords(
                studentService,
                "No students found.",
                "Student records:",
                Main::displayPersonDetails
        );
    }

    private static void searchStudent(Scanner scanner) {
        String studentId = readRequiredText(scanner, "Enter student USN: ");
        Student student = studentService.findStudent(studentId);

        if (student == null) {
            System.out.println("Student not found.");
        } else {
            System.out.println("Student found:");
            displayPersonDetails(student);
        }
    }

    private static void updateStudent(Scanner scanner) {
        String currentId = readRequiredText(scanner, "Enter the student USN to update: ");

        if (studentService.findStudent(currentId) == null) {
            System.out.println("Student not found.");
            return;
        }

        try {
            System.out.println("Enter the updated student details:");

            Student updatedStudent = readStudentDetails(scanner);

            if (studentService.updateStudent(currentId, updatedStudent)) {
                System.out.println("Student updated successfully.");
            }
        } catch (IllegalArgumentException exception) {
            System.out.println("Error: " + exception.getMessage());
        }
    }

    private static void deleteStudent(Scanner scanner) {
        String studentId = readRequiredText(scanner, "Enter student USN to delete: ");

        if (studentService.deleteStudent(studentId)) {
            System.out.println("Student deleted successfully.");
        } else {
            System.out.println("Student not found.");
        }
    }

    // FACULTY MANAGEMENT

    private static void facultyManagementMenu(Scanner scanner) {
        boolean inFacultyMenu = true;

        while (inFacultyMenu) {
            displayFacultyMenu();
            int choice = readMenuChoice(scanner, 1, 6);

            try {
                switch (choice) {
                    case 1:
                        addFaculty(scanner);
                        break;
                    case 2:
                        viewAllFaculty();
                        break;
                    case 3:
                        searchFaculty(scanner);
                        break;
                    case 4:
                        updateFaculty(scanner);
                        break;
                    case 5:
                        deleteFaculty(scanner);
                        break;
                    case 6:
                        inFacultyMenu = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number from 1 to 6.");
                }
            } catch (DatabaseException exception) {
                System.out.println("Database error: " + exception.getMessage());
            }

            System.out.println();
        }
    }

    private static void displayFacultyMenu() {
        System.out.println("=== FACULTY MANAGEMENT ===");
        System.out.println("1. Add Faculty");
        System.out.println("2. View All Faculty");
        System.out.println("3. Search Faculty");
        System.out.println("4. Update Faculty");
        System.out.println("5. Delete Faculty");
        System.out.println("6. Return to Main Menu");
        System.out.print("Enter your choice: ");
    }

    private static void addFaculty(Scanner scanner) {
        try {
            Faculty faculty = readFacultyDetails(scanner);
            facultyService.addFaculty(faculty);
            System.out.println("Faculty added successfully.");
        } catch (IllegalArgumentException exception) {
            System.out.println("Error: " + exception.getMessage());
        }
    }

    private static void viewAllFaculty() {
        displayRecords(
                facultyService,
                "No faculty records found.",
                "Faculty records:",
                Main::displayPersonDetails
        );
    }

    private static void searchFaculty(Scanner scanner) {
        String facultyId = readRequiredText(scanner, "Enter faculty ID: ");
        Faculty faculty = facultyService.findFaculty(facultyId);

        if (faculty == null) {
            System.out.println("Faculty member not found.");
        } else {
            System.out.println("Faculty member found:");
            displayPersonDetails(faculty);
        }
    }

    private static void updateFaculty(Scanner scanner) {
        String currentId = readRequiredText(scanner, "Enter the faculty ID to update: ");

        if (facultyService.findFaculty(currentId) == null) {
            System.out.println("Faculty member not found.");
            return;
        }

        try {
            System.out.println("Enter the updated faculty details:");

            Faculty updatedFaculty = readFacultyDetails(scanner);

            if (facultyService.updateFaculty(currentId, updatedFaculty)) {
                System.out.println("Faculty updated successfully.");
            }
        } catch (IllegalArgumentException exception) {
            System.out.println("Error: " + exception.getMessage());
        }
    }

    private static void deleteFaculty(Scanner scanner) {
        String facultyId = readRequiredText(scanner, "Enter faculty ID to delete: ");

        if (facultyService.deleteFaculty(facultyId)) {
            System.out.println("Faculty deleted successfully.");
        } else {
            System.out.println("Faculty member not found.");
        }
    }

    // COURSE MANAGEMENT

    private static void courseManagementMenu(Scanner scanner) {
        boolean inCourseMenu = true;

        while (inCourseMenu) {
            displayCourseMenu();
            int choice = readMenuChoice(scanner, 1, 6);

            try {
                switch (choice) {
                    case 1:
                        addCourse(scanner);
                        break;
                    case 2:
                        viewAllCourses();
                        break;
                    case 3:
                        searchCourse(scanner);
                        break;
                    case 4:
                        updateCourse(scanner);
                        break;
                    case 5:
                        deleteCourse(scanner);
                        break;
                    case 6:
                        inCourseMenu = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number from 1 to 6.");
                }
            } catch (DatabaseException exception) {
                System.out.println("Database error: " + exception.getMessage());
            }

            System.out.println();
        }
    }

    private static void displayCourseMenu() {
        System.out.println("=== COURSE MANAGEMENT ===");
        System.out.println("1. Add Course");
        System.out.println("2. View All Courses");
        System.out.println("3. Search Course");
        System.out.println("4. Update Course");
        System.out.println("5. Delete Course");
        System.out.println("6. Return to Main Menu");
        System.out.print("Enter your choice: ");
    }

    private static void addCourse(Scanner scanner) {
        try {
            Course course = readCourseDetails(scanner);
            courseService.addCourse(course);
            System.out.println("Course added successfully.");
        } catch (IllegalArgumentException exception) {
            System.out.println("Error: " + exception.getMessage());
        }
    }

    private static void viewAllCourses() {
        displayRecords(
                courseService,
                "No courses found.",
                "Course records:",
                Course::displayDetails
        );
    }

    private static void searchCourse(Scanner scanner) {
        String courseId = readRequiredText(scanner, "Enter the course ID: ");
        Course course = courseService.findCourse(courseId);

        if (course == null) {
            System.out.println("Course not found.");
        } else {
            System.out.println("Course found:");
            course.displayDetails();
        }
    }

    private static void updateCourse(Scanner scanner) {
        String currentId = readRequiredText(scanner, "Enter the course ID to update: ");

        if (courseService.findCourse(currentId) == null) {
            System.out.println("Course not found.");
            return;
        }

        try {
            System.out.println("Enter the updated course details:");

            Course updatedCourse = readCourseDetails(scanner);

            if (courseService.updateCourse(currentId, updatedCourse)) {
                System.out.println("Course updated successfully.");
            }
        } catch (IllegalArgumentException exception) {
            System.out.println("Error: " + exception.getMessage());
        }
    }

    private static void deleteCourse(Scanner scanner) {
        String courseId = readRequiredText(scanner, "Enter the course ID to delete: ");

        if (courseService.deleteCourse(courseId)) {
            System.out.println("Course deleted successfully.");
        } else {
            System.out.println("Course not found.");
        }
    }

    // INPUT METHODS

    private static Student readStudentDetails(Scanner scanner) {
        String studentId = readRequiredText(scanner, "Student USN: ");
        String studentName = readRequiredText(scanner, "Student Name: ");
        String email = readRequiredText(scanner, "Student Email: ");
        String branch = readRequiredText(scanner, "Department/Branch: ");
        String section = readSection(scanner);
        int semester = readSemester(scanner);

        return new Student(
                studentId,
                studentName,
                email,
                branch,
                section,
                semester
        );
    }

    private static Faculty readFacultyDetails(Scanner scanner) {
        String facultyId = readRequiredText(scanner, "Faculty ID: ");
        String facultyName = readRequiredText(scanner, "Faculty Name: ");
        String department = readRequiredText(scanner, "Department: ");
        String email = readRequiredText(scanner, "Email: ");
        String phoneNumber = readRequiredText(scanner, "Phone Number: ");

        return new Faculty(
                facultyId,
                facultyName,
                department,
                email,
                phoneNumber
        );
    }

    private static Course readCourseDetails(Scanner scanner) {
        String courseId = readRequiredText(scanner, "Course ID: ");
        String courseName = readRequiredText(scanner, "Course Name: ");
        String courseCode = readRequiredText(scanner, "Course Code: ");
        int credits = readCredits(scanner);
        String department = readRequiredText(scanner, "Department: ");

        return new Course(
                courseId,
                courseName,
                courseCode,
                credits,
                department
        );
    }

    // COMMON METHODS

    private static <T> void displayRecords(
            ManagementOperations<T> service,
            String emptyMessage,
            String heading,
            Consumer<T> displayRecord) {

        List<T> records = service.getAllRecords();

        if (records.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }

        System.out.println(heading);

        for (T record : records) {
            displayRecord.accept(record);
        }
    }

    private static void displayPersonDetails(Person person) {
        person.displayDetails();
    }

    private static String readRequiredText(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (!input.isEmpty()) {
                return input;
            }

            System.out.println("This field cannot be empty.");
        }
    }

    private static String readSection(Scanner scanner) {
        while (true) {
            System.out.print("Section (A, B, or C): ");
            String section = scanner.nextLine().trim();

            if (section.equalsIgnoreCase("A")
                    || section.equalsIgnoreCase("B")
                    || section.equalsIgnoreCase("C")) {
                return section.toUpperCase();
            }

            System.out.println("Invalid section. Please enter A, B, or C.");
        }
    }

    private static int readSemester(Scanner scanner) {
        while (true) {
            System.out.print("Semester: ");
            String input = scanner.nextLine().trim();

            try {
                int semester = Integer.parseInt(input);

                if (semester > 0) {
                    return semester;
                }
            } catch (NumberFormatException exception) {
                // Ask again below for non-numeric input.
            }

            System.out.println("Invalid semester. Please enter a positive whole number.");
        }
    }

    private static int readCredits(Scanner scanner) {
        while (true) {
            System.out.print("Credits: ");
            String input = scanner.nextLine().trim();

            try {
                int credits = Integer.parseInt(input);

                if (credits > 0) {
                    return credits;
                }
            } catch (NumberFormatException exception) {
                // Ask again below for non-numeric input.
            }

            System.out.println("Invalid credits. Please enter a positive whole number.");
        }
    }
}
