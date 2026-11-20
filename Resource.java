
public final class Resource {
    private String resourceId;
    private String resourceName;
    private String resourceType;
    private String buildingName;
    private int floorNumber;
    private String roomNumber;
    private int capacity;
    private String availabilityStatus;
    private String maintenanceNotes;

    // Existing constructor retained for compatibility
    public Resource(String resourceId, String resourceName,
                    String resourceType, String buildingName,
                    int capacity, String availabilityStatus) {
        this(resourceId, resourceName, resourceType, buildingName,
                1, "", capacity, availabilityStatus, "");
    }

    // Extended constructor for database resources
    public Resource(String resourceId, String resourceName,
                    String resourceType, String buildingName,
                    int floorNumber, String roomNumber,
                    int capacity, String availabilityStatus,
                    String maintenanceNotes) {

        setResourceId(resourceId);
        setResourceName(resourceName);
        setResourceType(resourceType);
        setBuildingName(buildingName);
        setFloorNumber(floorNumber);
        setRoomNumber(roomNumber);
        setCapacity(capacity);
        setAvailabilityStatus(availabilityStatus);
        setMaintenanceNotes(maintenanceNotes);
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = requireText(resourceId, "Resource ID");
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = requireText(resourceName, "Resource name");
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = ResourceType.parse(resourceType).getDisplayName();
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = requireText(buildingName, "Building name");
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(int floorNumber) {
        if (floorNumber < 0) {
            throw new IllegalArgumentException("Floor number cannot be negative.");
        }
        this.floorNumber = floorNumber;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber == null ? "" : roomNumber.trim();
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity cannot be negative.");
        }
        this.capacity = capacity;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        if (availabilityStatus == null
                || (!availabilityStatus.equalsIgnoreCase("Available")
                && !availabilityStatus.equalsIgnoreCase("Unavailable"))) {
            throw new IllegalArgumentException(
                    "Status must be Available or Unavailable.");
        }
        this.availabilityStatus =
                availabilityStatus.equalsIgnoreCase("Available")
                        ? "Available" : "Unavailable";
    }

    public String getMaintenanceNotes() {
        return maintenanceNotes;
    }

    public void setMaintenanceNotes(String maintenanceNotes) {
        this.maintenanceNotes =
                maintenanceNotes == null ? "" : maintenanceNotes.trim();
    }

    public boolean isAvailable() {
        return availabilityStatus.equals("Available");
    }

    public void displayResource() {
        System.out.println("-----------------------------------");
        System.out.println("Resource ID: " + resourceId);
        System.out.println("Name: " + resourceName);
        System.out.println("Type: " + resourceType);
        System.out.println("Building: " + buildingName);
        System.out.println("Floor: " + floorNumber);
        System.out.println("Room: " + roomNumber);
        System.out.println("Capacity: " + capacity);
        System.out.println("Status: " + availabilityStatus);
        System.out.println("Maintenance Notes: " + maintenanceNotes);
        System.out.println("-----------------------------------");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be empty.");
        }
        return value.trim();
    }

    public enum ResourceType {
        CLASSROOM("Classroom"),
        LABORATORY("Laboratory"),
        EQUIPMENT("Equipment"),
        SEMINAR_HALL("Seminar Hall");

        private final String displayName;

        ResourceType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static ResourceType parse(String value) {
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Resource type cannot be empty.");
            }

            String normalized = value.trim()
                    .replace(' ', '_')
                    .replace('-', '_');

            for (ResourceType type : values()) {
                if (type.name().equalsIgnoreCase(normalized)
                        || type.displayName.equalsIgnoreCase(value.trim())) {
                    return type;
                }
            }

            throw new IllegalArgumentException(
                    "Invalid resource type. Use Classroom, Laboratory, "
                    + "Equipment or Seminar Hall.");
        }
    }
}
