package model;

public enum CollaboratorCategory {
    SENIOR(2),
    INTERMEDIATE(5),
    JUNIOR(10);

    private final int defaultOpenTaskLimit;
    private int openTaskLimit;

    CollaboratorCategory(int openTaskLimit) {
        this.defaultOpenTaskLimit = openTaskLimit;
        this.openTaskLimit = openTaskLimit;
    }

    public int getOpenTaskLimit() {
        return openTaskLimit;
    }

    public int getDefaultOpenTaskLimit() {
        return defaultOpenTaskLimit;
    }

    public void setOpenTaskLimit(int openTaskLimit) {
        if (openTaskLimit <= 0) {
            throw new IllegalArgumentException("Limit must be a positive integer.");
        }
        this.openTaskLimit = openTaskLimit;
    }

    public void resetOpenTaskLimit() {
        this.openTaskLimit = defaultOpenTaskLimit;
    }

    public static CollaboratorCategory fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return CollaboratorCategory.valueOf(value.trim().toUpperCase());
    }
}
