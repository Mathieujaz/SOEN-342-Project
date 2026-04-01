package model;

public class Collaborator {
    private final String name;
    private final long openTaskCount;

    public Collaborator(String name, long openTaskCount) {
        this.name = name;
        this.openTaskCount = openTaskCount;
    }

    public String getName() {
        return name;
    }

    public long getOpenTaskCount() {
        return openTaskCount;
    }
}
