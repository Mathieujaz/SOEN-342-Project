package model;

public class Tag {
    private final String name;

    public Tag(String name) {
        this.name = name == null ? "" : name.trim();
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
