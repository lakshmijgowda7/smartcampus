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

    public ResourceBookingService(ResourceService resourceService) {
        if (resourceService == null) {
            throw new IllegalArgumentException("Resource service cannot be null.");
        }
        this.resourceService = resourceService;
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
                default:
                    System.out.println("Invalid choice. Enter a number from 1 to 4.");
            }
        }
    }

    public synchronized boolean bookResource(String resourceId,
                                             LocalDateTime startTime,
                                             LocalDateTime endTime) {
        if (resourceId == null || resourceId.trim().isEmpty()) {
            System.out.println("Error: Resource ID cannot be empty.");
            return false;
        }
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            System.out.println("Error: Booking end time must be after its start time.");
            return false;
        }
        if (startTime.isBefore(LocalDateTime.now())) {
            System.out.println("Error: A resource cannot be booked for a past time.");
            return false;
        }

        synchronized (resourceService) {
            Resource resource = resourceService.findResourceById(resourceId);
            if (resource == null) {
                System.out.println("Error: Resource not found.");
                return false;
            }
            if (!resource.isAvailable()) {
                System.out.println("Error: Resource is marked unavailable.");
                return false;
            }

            for (ResourceBooking booking : bookings) {
                if (booking.getResourceId().equalsIgnoreCase(resource.getResourceId())
                        && booking.overlaps(startTime, endTime)) {
                    System.out.println("Error: Resource already has an overlapping booking.");
                    return false;
                }
            }

            String bookingId = "BKG-" + nextBookingNumber.getAndIncrement();
            bookings.add(new ResourceBooking(bookingId, resource, startTime, endTime));
            System.out.println("Booking created successfully. Booking ID: " + bookingId);
            return true;
        }
    }

    public synchronized void viewBookings() {
        if (bookings.isEmpty()) {
            System.out.println("No bookings have been made.");
            return;
        }
        for (ResourceBooking booking : bookings) {
            booking.displayBooking();
        }
    }

    public synchronized boolean cancelBooking(String bookingId) {
        ResourceBooking booking = findBooking(bookingId);
        if (booking == null) {
            System.out.println("Error: Booking not found.");
            return false;
        }
        if (booking.isCancelled()) {
            System.out.println("Error: Booking has already been cancelled.");
            return false;
        }

        booking.cancel();
        System.out.println("Booking cancelled successfully.");
        return true;
    }

    public synchronized int getBookingCount() {
        return bookings.size();
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

    private ResourceBooking findBooking(String bookingId) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            return null;
        }
        for (ResourceBooking booking : bookings) {
            if (booking.getBookingId().equalsIgnoreCase(bookingId.trim())) {
                return booking;
            }
        }
        return null;
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
