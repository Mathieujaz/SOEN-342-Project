package model;

public class Task {
    private int id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private String dueDate;

    public Task(String title, String description, TaskStatus status, Priority priority, String dueDate) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    public Task(int id, String title, String description, TaskStatus status, Priority priority, String dueDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TaskStatus getStatus() { return status; }
    public Priority getPriority() { return priority; }
    public String getDueDate() { return dueDate; }

    @Override
    public String toString() {
        return id + " | " + title + " | " + status + " | " + priority;
    }
}