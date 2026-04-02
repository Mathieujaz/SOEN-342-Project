package model;

public class Subtask {
    private final String title;
    private final boolean completed;

    public Subtask(String title, boolean completed) {
        this.title = title;
        this.completed = completed;
    }

    public String getTitle() {
        return title;
    }

    public boolean isCompleted() {
        return completed;
    }

    @Override
    public String toString() {
        return (completed ? "[x] " : "[ ] ") + title;
    }
}
