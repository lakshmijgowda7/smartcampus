public final class CampusEvent {
    private final String eventId;
    private String eventName;
    private String date;
    private String venue;
    private String description;

    public CampusEvent(String eventId, String eventName, String date,
                       String venue, String description) {
        this.eventId = requireText(eventId, "Event ID");
        setEventName(eventName);
        setDate(date);
        setVenue(venue);
        setDescription(description);
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = requireText(eventName, "Event name");
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = requireText(date, "Event date");
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = requireText(venue, "Event venue");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = requireText(description, "Event description");
    }

    public void displayEvent() {
        System.out.println("Event ID: " + eventId
                + " | Name: " + eventName
                + " | Date: " + date
                + " | Venue: " + venue
                + " | Description: " + description);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
        return value.trim();
    }
}
