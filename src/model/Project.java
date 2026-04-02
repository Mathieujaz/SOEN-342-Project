package model;

public class Project {
    private final String name;
    private final String description;

    public Project(String name, String description) {
        this.name = name == null ? "" : name.trim();
        this.description = description == null ? "" : description.trim();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name + (description.isBlank() ? "" : " | " + description);
    }
}
