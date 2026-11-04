import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class ResourceBookingServiceTest {
    public static void main(String[] args) throws InterruptedException {
        ResourceService resources = new ResourceService();
        resources.addResource(new Scanner(
                "ROOM1\nLecture Room\nClassroom\nNorth Hall\n40\nAvailable\n"));
        resources.addResource(new Scanner(
                "LAB1\nChemistry Lab\nLaboratory\nScience Hall\n25\nUnavailable\n"));

        testBookingAndCancellation(resources);
        testSimultaneousBookings(resources);
        testBookingMenu(resources);

        System.out.println("Resource booking tests passed.");
    }

    private static void testBookingAndCancellation(ResourceService resources) {
        ResourceBookingService bookings = new ResourceBookingService(resources);
        LocalDateTime start = LocalDateTime.now().plusDays(2).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);

        if (!bookings.bookResource("ROOM1", start, end)) {
            throw new AssertionError("Expected first booking to succeed.");
        }
        if (bookings.bookResource("ROOM1", start.plusMinutes(30), end.plusMinutes(30))) {
            throw new AssertionError("Overlapping booking was accepted.");
        }
        if (!bookings.bookResource("ROOM1", end, end.plusHours(1))) {
            throw new AssertionError("Back-to-back booking should be accepted.");
        }
        if (bookings.bookResource("LAB1", start, end)) {
            throw new AssertionError("Unavailable resource was booked.");
        }
        if (bookings.bookResource("MISSING", start, end)) {
            throw new AssertionError("Nonexistent resource was booked.");
        }
        if (bookings.bookResource("ROOM1", end, end)) {
            throw new AssertionError("Invalid time range was accepted.");
        }
        if (bookings.bookResource("ROOM1", start.minusDays(3), start.minusDays(2))) {
            throw new AssertionError("Past booking was accepted.");
        }
        if (!bookings.cancelBooking("BKG-1")) {
            throw new AssertionError("Expected cancellation to succeed.");
        }
        if (bookings.cancelBooking("BKG-1")) {
            throw new AssertionError("Already-cancelled booking was cancelled again.");
        }
        if (!bookings.bookResource("ROOM1", start, end)) {
            throw new AssertionError("Cancelled time slot should be reusable.");
        }
        if (bookings.cancelBooking("MISSING")) {
            throw new AssertionError("Nonexistent booking was cancelled.");
        }
    }

    private static void testSimultaneousBookings(ResourceService resources)
            throws InterruptedException {
        ResourceBookingService bookings = new ResourceBookingService(resources);
        LocalDateTime start = LocalDateTime.now().plusDays(3).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch startTogether = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger failures = new AtomicInteger();

        Runnable attempt = () -> {
            ready.countDown();
            try {
                startTogether.await();
                if (bookings.bookResource("ROOM1", start, end)) {
                    successes.incrementAndGet();
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                failures.incrementAndGet();
            }
        };

        Thread first = new Thread(attempt, "booking-attempt-1");
        Thread second = new Thread(attempt, "booking-attempt-2");
        first.start();
        second.start();
        ready.await();
        startTogether.countDown();
        first.join();
        second.join();

        if (failures.get() != 0 || successes.get() != 1 || bookings.getBookingCount() != 1) {
            throw new AssertionError("Simultaneous booking attempts were not safely serialized.");
        }
    }

    private static void testBookingMenu(ResourceService resources) {
        ResourceBookingService bookings = new ResourceBookingService(resources);
        LocalDateTime start = LocalDateTime.now().plusDays(4).withSecond(0).withNano(0);
        String input = String.join("\n",
                "1", "ROOM1", start.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                start.plusHours(1).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                "2", "3", "BKG-1", "2", "4", "");
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try {
            System.setOut(new PrintStream(captured));
            bookings.runMenu(new Scanner(input));
        } finally {
            System.setOut(originalOutput);
        }

        String output = captured.toString();
        if (!output.contains("Booking created successfully.")
                || !output.contains("Booking ID: BKG-1")
                || !output.contains("Booking cancelled successfully.")
                || !output.contains("Status: Cancelled")) {
            throw new AssertionError("Booking console menu did not create, display, and cancel a booking.");
        }
    }
}
