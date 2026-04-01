package model;

public class Collaborator {
    private final String name;
    private final CollaboratorCategory category;
    private final long openTaskCount;
    private final int openTaskLimit;

    public Collaborator(String name, CollaboratorCategory category, long openTaskCount) {
        this.name = name;
        this.category = category;
        this.openTaskCount = openTaskCount;
        this.openTaskLimit = category == null ? Integer.MAX_VALUE : category.getOpenTaskLimit();
    }

    public String getName() {
        return name;
    }

    public CollaboratorCategory getCategory() {
        return category;
    }

    public long getOpenTaskCount() {
        return openTaskCount;
    }

    public int getOpenTaskLimit() {
        return openTaskLimit;
    }

    public boolean isOverloaded() {
        return category != null && openTaskCount > openTaskLimit;
    }
}
