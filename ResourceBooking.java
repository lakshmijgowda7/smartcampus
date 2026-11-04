import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ResourceBooking {
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String bookingId;
    private final String resourceId;
    private final String resourceName;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private boolean cancelled;

    public ResourceBooking(String bookingId, Resource resource,
                           LocalDateTime startTime, LocalDateTime endTime) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            throw new IllegalArgumentException("Booking ID cannot be empty.");
        }
        if (resource == null) {
            throw new IllegalArgumentException("Resource cannot be null.");
        }
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Booking end time must be after its start time.");
        }

        this.bookingId = bookingId.trim();
        this.resourceId = resource.getResourceId();
        this.resourceName = resource.getResourceName();
        this.startTime = startTime;
        this.endTime = endTime;
        this.cancelled = false;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        cancelled = true;
    }

    public boolean overlaps(LocalDateTime requestedStart, LocalDateTime requestedEnd) {
        return !cancelled && requestedStart.isBefore(endTime) && startTime.isBefore(requestedEnd);
    }

    public void displayBooking() {
        System.out.println("Booking ID: " + bookingId
                + " | Resource ID: " + resourceId
                + " | Resource: " + resourceName
                + " | Start: " + startTime.format(DISPLAY_FORMAT)
                + " | End: " + endTime.format(DISPLAY_FORMAT)
                + " | Status: " + (cancelled ? "Cancelled" : "Active"));
    }
}
