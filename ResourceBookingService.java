import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;

public class ResourceBookingService {
    private static final DateTimeFormatter INPUT_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm")
                    .withResolverStyle(ResolverStyle.STRICT);

    private final ResourceService resourceService;
    private final ArrayList<ResourceBooking> bookings = new ArrayList<>();
    private final AtomicLong nextBookingNumber = new AtomicLong(1);
    private long currentUserId = 1L;
    private UserRole currentRole = UserRole.ADMINISTRATOR;

    public ResourceBookingService(ResourceService resourceService) {
        this(resourceService, 1L);
    }

    public ResourceBookingService(ResourceService resourceService, long currentUserId) {
        if (resourceService == null) {
            throw new IllegalArgumentException("Resource service cannot be null.");
        }
        this.resourceService = resourceService;
        this.currentUserId = currentUserId > 0 ? currentUserId : 1L;
        refreshBookingCache();
    }

    public void setCurrentUserId(long currentUserId) {
        this.currentUserId = currentUserId > 0 ? currentUserId : 1L;
    }

    public void setCurrentUserRole(UserRole currentRole) {
        this.currentRole = currentRole == null ? UserRole.ADMINISTRATOR : currentRole;
    }

    public void runMenu(Scanner scanner) {
        if (scanner == null) {
            throw new IllegalArgumentException("Scanner cannot be null.");
        }

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("=== Resource Booking ===");
            System.out.println("1. Book Resource");
            System.out.println("2. View Bookings");
            System.out.println("3. Cancel Booking");
            System.out.println("4. Back to Main Menu");
            System.out.println("5. Approve Booking");
            System.out.println("6. Reject Booking");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    bookFromConsole(scanner);
                    break;
                case "2":
                    viewBookings();
                    break;
                case "3":
                    cancelBooking(scanner);
                    break;
                case "4":
                    running = false;
                    break;
                case "5":
                    approveBooking(scanner);
                    break;
                case "6":
                    rejectBooking(scanner);
                    break;
                default:
                    System.out.println("Invalid choice. Enter a number from 1 to 6.");
            }
        }
    }

    public synchronized boolean bookResource(String resourceId,
                                             LocalDateTime startTime,
                                             LocalDateTime endTime) {
        return bookResource(resourceId, startTime, endTime, currentRole, currentUserId);
    }

    public synchronized boolean bookResource(String resourceId,
                                             LocalDateTime startTime,
                                             LocalDateTime endTime,
                                             UserRole actorRole,
                                             long userId) {
        if (resourceId == null || resourceId.trim().isEmpty()) {
            System.out.println("Error: Resource ID cannot be empty.");
            return false;
        }
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            System.out.println("Error: Booking end time must be after its start time.");
            return false;
        }
        if (!startTime.toLocalDate().equals(endTime.toLocalDate())) {
            System.out.println("Error: Booking must start and end on the same day.");
            return false;
        }
        if (startTime.isBefore(LocalDateTime.now())) {
            System.out.println("Error: A resource cannot be booked for a past time.");
            return false;
        }
        if (userId <= 0) {
            System.out.println("Error: A valid user is required to book a resource.");
            return false;
        }
        if (actorRole == null) {
            actorRole = currentRole;
        }
        if (actorRole != UserRole.ADMINISTRATOR
                && actorRole != UserRole.FACULTY
                && actorRole != UserRole.STUDENT) {
            System.out.println("Error: Only students, faculty, and administrators may book resources.");
            return false;
        }

        this.currentRole = actorRole;
        this.currentUserId = userId;

        Resource resource = resourceService.findResourceById(resourceId);
        if (resource == null) {
            System.out.println("Error: Resource not found.");
            return false;
        }
        if (!resource.isAvailable()) {
            System.out.println("Error: Resource is marked unavailable.");
            return false;
        }

        synchronized (resourceService) {
            for (ResourceBooking booking : bookings) {
                if (booking.getResourceId().equalsIgnoreCase(resource.getResourceId())
                        && booking.overlaps(startTime, endTime)) {
                    System.out.println("Error: Resource already has an overlapping booking.");
                    return false;
                }
            }
        }

        long resourceDatabaseId = resolveResourceDatabaseId(resourceId);
        if (resourceDatabaseId <= 0) {
            System.out.println("Error: Resource not found in the database.");
            return false;
        }

        long effectiveUserId = resolveCurrentUserId();
        if (!databaseOverlapExists(resourceDatabaseId, startTime, endTime)) {
            String insertSql = "INSERT INTO bookings "
                    + "(resource_id, user_id, booking_date, start_time, end_time, purpose, "
                    + "status, rejection_reason, approver_notes, approved_by_user_id, "
                    + "created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, 'PENDING', NULL, NULL, NULL, "
                    + "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

                statement.setLong(1, resourceDatabaseId);
                statement.setLong(2, effectiveUserId);
                statement.setDate(3, Date.valueOf(startTime.toLocalDate()));
                statement.setTime(4, Time.valueOf(startTime.toLocalTime()));
                statement.setTime(5, Time.valueOf(endTime.toLocalTime()));
                statement.setString(6, "Requested resource booking");

                int updated = statement.executeUpdate();
                if (updated == 0) {
                    System.out.println("Error: Booking could not be created.");
                    return false;
                }

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        String bookingId = "BKG-" + generatedKeys.getLong(1);
                        bookings.add(new ResourceBooking(bookingId, resource, startTime, endTime));
                        System.out.println("Booking created successfully. Booking ID: " + bookingId);
                        return true;
                    }
                }
            } catch (SQLException exception) {
                System.out.println("Could not create booking: " + exception.getMessage());
                return false;
            }
        }

        System.out.println("Error: Resource already has an overlapping booking.");
        return false;
    }

    public synchronized void viewBookings() {
        refreshBookingCache();
        if (bookings.isEmpty()) {
            System.out.println("No bookings have been made.");
            return;
        }
        for (ResourceBooking booking : bookings) {
            booking.displayBooking();
        }
    }

    public synchronized void viewBookingsForUser(long userId, UserRole userRole) {
        if (userId <= 0) {
            System.out.println("Error: A valid user is required to view bookings.");
            return;
        }

        if (userRole == null) {
            userRole = currentRole;
        }

        String sql = "SELECT b.id, r.resource_code, r.name, b.booking_date, b.start_time, b.end_time, "
                + "b.status FROM bookings b JOIN resources r ON r.id = b.resource_id "
                + "WHERE b.user_id = ? ORDER BY b.id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);

            try (ResultSet result = statement.executeQuery()) {
                boolean found = false;
                while (result.next()) {
                    found = true;
                    Resource resource = new Resource(
                            result.getString("resource_code"),
                            result.getString("name"),
                            "Classroom",
                            "Main",
                            1,
                            "Available");
                    LocalDateTime start = LocalDateTime.of(
                            result.getDate("booking_date").toLocalDate(),
                            result.getTime("start_time").toLocalTime());
                    LocalDateTime end = LocalDateTime.of(
                            result.getDate("booking_date").toLocalDate(),
                            result.getTime("end_time").toLocalTime());
                    ResourceBooking booking = new ResourceBooking(
                            "BKG-" + result.getLong("id"),
                            resource,
                            start,
                            end,
                            result.getString("status"),
                            userId,
                            -1L);
                    booking.displayBooking();
                }
                if (!found) {
                    System.out.println("No bookings found for this user.");
                }
            }
        } catch (SQLException exception) {
            System.out.println("Could not load bookings for this user: " + exception.getMessage());
        }
    }

    public synchronized boolean cancelBooking(String bookingId) {
        String parsedBookingId = normalizeBookingId(bookingId);
        if (parsedBookingId == null) {
            System.out.println("Error: Booking not found.");
            return false;
        }

        String sql = "SELECT id, status FROM bookings WHERE id = ? OR CONCAT('BKG-', id) = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, extractBookingDatabaseId(parsedBookingId));
            statement.setString(2, parsedBookingId);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    System.out.println("Error: Booking not found.");
                    return false;
                }

                String status = result.getString("status");
                if ("CANCELLED".equalsIgnoreCase(status)) {
                    System.out.println("Error: Booking has already been cancelled.");
                    return false;
                }

                String updateSql = "UPDATE bookings SET status = 'CANCELLED', updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                    update.setLong(1, result.getLong("id"));
                    update.executeUpdate();
                }
            }
        } catch (SQLException exception) {
            System.out.println("Could not cancel booking: " + exception.getMessage());
            return false;
        }

        refreshBookingCache();
        System.out.println("Booking cancelled successfully.");
        return true;
    }

    public synchronized boolean approveBooking(String bookingId, String approverNotes) {
        if (currentRole != UserRole.ADMINISTRATOR) {
            System.out.println("Error: Only administrators can approve bookings.");
            return false;
        }
        return approveBooking(bookingId, approverNotes, currentRole, currentUserId);
    }

    public synchronized boolean approveBooking(String bookingId,
                                              String approverNotes,
                                              UserRole actorRole,
                                              long approverUserId) {
        if (actorRole != UserRole.ADMINISTRATOR) {
            System.out.println("Error: Only administrators can approve bookings.");
            return false;
        }
        if (approverUserId <= 0) {
            System.out.println("Error: A valid administrator user is required to approve bookings.");
            return false;
        }

        this.currentRole = actorRole;
        this.currentUserId = approverUserId;

        String parsedBookingId = normalizeBookingId(bookingId);
        if (parsedBookingId == null) {
            System.out.println("Error: Booking not found.");
            return false;
        }

        String sql = "SELECT id, status FROM bookings WHERE id = ? OR CONCAT('BKG-', id) = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, extractBookingDatabaseId(parsedBookingId));
            statement.setString(2, parsedBookingId);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    System.out.println("Error: Booking not found.");
                    return false;
                }

                long bookingDatabaseId = result.getLong("id");
                String status = result.getString("status");
                if ("APPROVED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)
                        || "REJECTED".equalsIgnoreCase(status)) {
                    System.out.println("Error: This booking cannot be approved in its current state.");
                    return false;
                }

                String updateSql = "UPDATE bookings SET status = 'APPROVED', approver_notes = ?, "
                        + "approved_by_user_id = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                    update.setString(1, approverNotes == null || approverNotes.trim().isEmpty()
                            ? "Approved by administrator." : approverNotes.trim());
                    update.setLong(2, approverUserId);
                    update.setLong(3, bookingDatabaseId);
                    update.executeUpdate();
                }
            }
        } catch (SQLException exception) {
            System.out.println("Could not approve booking: " + exception.getMessage());
            return false;
        }

        refreshBookingCache();
        System.out.println("Booking approved successfully.");
        return true;
    }

    public synchronized boolean rejectBooking(String bookingId, String rejectionReason) {
        if (currentRole != UserRole.ADMINISTRATOR) {
            System.out.println("Error: Only administrators can reject bookings.");
            return false;
        }
        return rejectBooking(bookingId, rejectionReason, currentRole, currentUserId);
    }

    public synchronized boolean rejectBooking(String bookingId,
                                             String rejectionReason,
                                             UserRole actorRole,
                                             long approverUserId) {
        if (actorRole != UserRole.ADMINISTRATOR) {
            System.out.println("Error: Only administrators can reject bookings.");
            return false;
        }
        if (approverUserId <= 0) {
            System.out.println("Error: A valid administrator user is required to reject bookings.");
            return false;
        }

        this.currentRole = actorRole;
        this.currentUserId = approverUserId;

        String parsedBookingId = normalizeBookingId(bookingId);
        if (parsedBookingId == null) {
            System.out.println("Error: Booking not found.");
            return false;
        }

        String sql = "SELECT id, status FROM bookings WHERE id = ? OR CONCAT('BKG-', id) = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, extractBookingDatabaseId(parsedBookingId));
            statement.setString(2, parsedBookingId);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    System.out.println("Error: Booking not found.");
                    return false;
                }

                long bookingDatabaseId = result.getLong("id");
                String status = result.getString("status");
                if ("APPROVED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)
                        || "REJECTED".equalsIgnoreCase(status)) {
                    System.out.println("Error: This booking cannot be rejected in its current state.");
                    return false;
                }

                String updateSql = "UPDATE bookings SET status = 'REJECTED', rejection_reason = ?, "
                        + "approved_by_user_id = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                    update.setString(1, rejectionReason == null || rejectionReason.trim().isEmpty()
                            ? "No reason provided." : rejectionReason.trim());
                    update.setLong(2, approverUserId);
                    update.setLong(3, bookingDatabaseId);
                    update.executeUpdate();
                }
            }
        } catch (SQLException exception) {
            System.out.println("Could not reject booking: " + exception.getMessage());
            return false;
        }

        refreshBookingCache();
        System.out.println("Booking rejected successfully.");
        return true;
    }

    public synchronized int getBookingCount() {
        refreshBookingCache();
        return bookings.size();
    }

    private long resolveCurrentUserId() {
        if (currentUserId > 0) {
            return currentUserId;
        }

        String sql = "SELECT id FROM users ORDER BY id LIMIT 1";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            if (result.next()) {
                return result.getLong("id");
            }
        } catch (SQLException exception) {
            return 1L;
        }
        return 1L;
    }

    private long resolveResourceDatabaseId(String resourceId) {
        String sql = "SELECT id FROM resources WHERE resource_code = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, resourceId.trim());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getLong("id");
                }
            }
        } catch (SQLException exception) {
            System.out.println("Could not find resource: " + exception.getMessage());
        }
        return -1L;
    }

    private boolean databaseOverlapExists(long resourceDatabaseId, LocalDateTime startTime, LocalDateTime endTime) {
        String sql = "SELECT 1 FROM bookings "
                + "WHERE resource_id = ? AND booking_date = ? AND status IN ('PENDING', 'APPROVED') "
                + "AND start_time < ? AND end_time > ? LIMIT 1";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, resourceDatabaseId);
            statement.setDate(2, Date.valueOf(startTime.toLocalDate()));
            statement.setTime(3, Time.valueOf(endTime.toLocalTime()));
            statement.setTime(4, Time.valueOf(startTime.toLocalTime()));

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException exception) {
            System.out.println("Could not check booking overlap: " + exception.getMessage());
            return true;
        }
    }

    private void refreshBookingCache() {
        bookings.clear();
        String sql = "SELECT b.id, r.resource_code, r.name, b.booking_date, b.start_time, b.end_time, "
                + "b.status FROM bookings b JOIN resources r ON r.id = b.resource_id ORDER BY b.id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                long databaseId = result.getLong("id");
                String resourceCode = result.getString("resource_code");
                String resourceName = result.getString("name");
                LocalDateTime start = LocalDateTime.of(
                        result.getDate("booking_date").toLocalDate(),
                        result.getTime("start_time").toLocalTime());
                LocalDateTime end = LocalDateTime.of(
                        result.getDate("booking_date").toLocalDate(),
                        result.getTime("end_time").toLocalTime());
                Resource resource = new Resource(
                        resourceCode,
                        resourceName,
                        "Classroom",
                        "Main",
                        1,
                        "Available"
                );
                String status = result.getString("status");
                ResourceBooking booking = new ResourceBooking(
                        "BKG-" + databaseId,
                        resource,
                        start,
                        end,
                        status,
                        -1L,
                        -1L);
                if ("CANCELLED".equalsIgnoreCase(status)) {
                    booking.cancel();
                }
                bookings.add(booking);
            }
        } catch (SQLException exception) {
            System.out.println("Could not load bookings: " + exception.getMessage());
        }
    }

    private void bookFromConsole(Scanner scanner) {
        String resourceId = readRequiredText(scanner, "Resource ID: ");
        LocalDateTime startTime = readDateTime(scanner, "Start (yyyy-MM-dd HH:mm): ");
        LocalDateTime endTime = readDateTime(scanner, "End (yyyy-MM-dd HH:mm): ");
        bookResource(resourceId, startTime, endTime);
    }

    private void cancelBooking(Scanner scanner) {
        cancelBooking(readRequiredText(scanner, "Booking ID to cancel: "));
    }

    private void approveBooking(Scanner scanner) {
        String bookingId = readRequiredText(scanner, "Booking ID to approve: ");
        String notes = readOptionalText(scanner, "Approval note (optional): ");
        approveBooking(bookingId, notes);
    }

    private void rejectBooking(Scanner scanner) {
        String bookingId = readRequiredText(scanner, "Booking ID to reject: ");
        String reason = readOptionalText(scanner, "Rejection reason (optional): ");
        rejectBooking(bookingId, reason);
    }

    private ResourceBooking findBooking(String bookingId) {
        refreshBookingCache();
        if (bookingId == null || bookingId.trim().isEmpty()) {
            return null;
        }
        String normalized = normalizeBookingId(bookingId);
        if (normalized == null) {
            return null;
        }
        for (ResourceBooking booking : bookings) {
            if (booking.getBookingId().equalsIgnoreCase(normalized)) {
                return booking;
            }
        }
        return null;
    }

    private String normalizeBookingId(String bookingId) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            return null;
        }

        String trimmed = bookingId.trim();
        if (trimmed.equalsIgnoreCase("BKG-0") || trimmed.matches("[0-9]+")) {
            return "BKG-" + trimmed.replaceAll("[^0-9]", "");
        }
        if (trimmed.toUpperCase().startsWith("BKG-")) {
            return trimmed.toUpperCase();
        }
        return "BKG-" + trimmed;
    }

    private long extractBookingDatabaseId(String bookingId) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            return -1L;
        }
        String normalized = bookingId.trim();
        if (normalized.toUpperCase().startsWith("BKG-")) {
            normalized = normalized.substring(4);
        }
        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException exception) {
            return -1L;
        }
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

    private String readOptionalText(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private LocalDateTime readDateTime(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            try {
                return LocalDateTime.parse(value, INPUT_FORMAT);
            } catch (DateTimeParseException exception) {
                System.out.println("Enter a valid date and time in yyyy-MM-dd HH:mm format.");
            }
        }
    }
}
