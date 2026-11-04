public final class Resource {
    private String resourceId;
    private String resourceName;
    private String resourceType;
    private String buildingName;
    private int capacity;
    private String availabilityStatus;

    public Resource(String resourceId, String resourceName, String resourceType,
                    String buildingName, int capacity, String availabilityStatus) {
        setResourceId(resourceId);
        setResourceName(resourceName);
        setResourceType(resourceType);
        setBuildingName(buildingName);
        setCapacity(capacity);
        setAvailabilityStatus(availabilityStatus);
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
        this.resourceType = ResourceType.parse(resourceType).displayName;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = requireText(buildingName, "Building name");
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be a positive number.");
        }
        this.capacity = capacity;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        if (availabilityStatus == null
                || !(availabilityStatus.trim().equalsIgnoreCase("Available")
                || availabilityStatus.trim().equalsIgnoreCase("Unavailable"))) {
            throw new IllegalArgumentException("Status must be Available or Unavailable.");
        }
        this.availabilityStatus = availabilityStatus.trim().equalsIgnoreCase("Available")
                ? "Available" : "Unavailable";
    }

    public boolean isAvailable() {
        return availabilityStatus.equals("Available");
    }

    public void displayResource() {
        System.out.println("Resource ID: " + resourceId
                + " | Name: " + resourceName
                + " | Type: " + resourceType
                + " | Building: " + buildingName
                + " | Capacity: " + capacity
                + " | Status: " + availabilityStatus);
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
        return value.trim();
    }
    public enum ResourceType {
        CLASSROOM("Classroom"),
        LABORATORY("Laboratory"),
        EQUIPMENT("Equipment");

        final String displayName;

        ResourceType(String displayName) {
            this.displayName = displayName;
        }

        static ResourceType parse(String value) {
            if (value != null) {
                for (ResourceType type : values()) {
                    if (type.displayName.equalsIgnoreCase(value.trim())) {
                        return type;
                    }
                }
            }
            throw new IllegalArgumentException(
                    "Resource type must be Classroom, Laboratory, or Equipment.");
        }
    }
}
