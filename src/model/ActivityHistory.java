package model;

public class ActivityHistory {
    private final String timestamp;
    private final String description;

    public ActivityHistory(String timestamp, String description) {
        this.timestamp = timestamp;
        this.description = description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return timestamp + " | " + description;
    }
}
