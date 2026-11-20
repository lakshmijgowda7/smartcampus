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
    private final long userId;
    private final long resourceDatabaseId;
    private String status;
    private boolean cancelled;

    public ResourceBooking(String bookingId, Resource resource,
                           LocalDateTime startTime, LocalDateTime endTime) {
        this(bookingId, resource, startTime, endTime, "PENDING", -1L, -1L);
    }

    public ResourceBooking(String bookingId, Resource resource,
                           LocalDateTime startTime, LocalDateTime endTime,
                           String status, long userId, long resourceDatabaseId) {
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
        this.userId = userId;
        this.resourceDatabaseId = resourceDatabaseId;
        this.status = status == null ? "PENDING" : status.toUpperCase();
        this.cancelled = "CANCELLED".equalsIgnoreCase(this.status);
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public long getUserId() {
        return userId;
    }

    public long getResourceDatabaseId() {
        return resourceDatabaseId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? "PENDING" : status.toUpperCase();
        this.cancelled = "CANCELLED".equalsIgnoreCase(this.status);
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
        setStatus("CANCELLED");
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
                + " | Status: " + (cancelled ? "Cancelled" : status));
    }
}
