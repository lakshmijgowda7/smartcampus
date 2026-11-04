import java.util.ArrayList;
import java.util.Scanner;

public class CampusActivityService {
    private final ArrayList<CampusEvent> events = new ArrayList<>();

    public void runMenu(Scanner scanner) {
        if (scanner == null) {
            throw new IllegalArgumentException("Scanner cannot be null.");
        }

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("=== Campus Activity Management ===");
            System.out.println("1. Add Event");
            System.out.println("2. View All Events");
            System.out.println("3. Search Event by ID");
            System.out.println("4. Update Event");
            System.out.println("5. Delete Event");
            System.out.println("6. Back to Main Menu");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    addEvent(scanner);
                    break;
                case "2":
                    viewAllEvents();
                    break;
                case "3":
                    searchEvent(scanner);
                    break;
                case "4":
                    updateEvent(scanner);
                    break;
                case "5":
                    deleteEvent(scanner);
                    break;
                case "6":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Enter a number from 1 to 6.");
            }
        }
    }

    public void addEvent(Scanner scanner) {
        String eventId = readRequiredText(scanner, "Event ID: ");
        if (findEvent(eventId) != null) {
            System.out.println("Error: An event with that ID already exists.");
            return;
        }

        CampusEvent event = new CampusEvent(
                eventId,
                readRequiredText(scanner, "Event name: "),
                readRequiredText(scanner, "Date: "),
                readRequiredText(scanner, "Venue: "),
                readRequiredText(scanner, "Description: "));
        events.add(event);
        System.out.println("Event added successfully.");
    }

    public void viewAllEvents() {
        if (events.isEmpty()) {
            System.out.println("No events have been added.");
            return;
        }

        for (CampusEvent event : events) {
            event.displayEvent();
        }
    }

    public void searchEvent(Scanner scanner) {
        CampusEvent event = findEvent(readRequiredText(scanner, "Event ID to search: "));
        if (event == null) {
            System.out.println("Error: Event not found.");
            return;
        }
        event.displayEvent();
    }

    public void updateEvent(Scanner scanner) {
        CampusEvent event = findEvent(readRequiredText(scanner, "Event ID to update: "));
        if (event == null) {
            System.out.println("Error: Event not found. Nothing was updated.");
            return;
        }

        event.setEventName(readRequiredText(scanner, "New event name: "));
        event.setDate(readRequiredText(scanner, "New date: "));
        event.setVenue(readRequiredText(scanner, "New venue: "));
        event.setDescription(readRequiredText(scanner, "New description: "));
        System.out.println("Event updated successfully.");
    }

    public void deleteEvent(Scanner scanner) {
        CampusEvent event = findEvent(readRequiredText(scanner, "Event ID to delete: "));
        if (event == null) {
            System.out.println("Error: Event not found. Nothing was deleted.");
            return;
        }

        events.remove(event);
        System.out.println("Event deleted successfully.");
    }

    private CampusEvent findEvent(String eventId) {
        for (CampusEvent event : events) {
            if (event.getEventId().equalsIgnoreCase(eventId.trim())) {
                return event;
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
}
