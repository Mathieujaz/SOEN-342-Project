package model;

public class TaskOccurence {
    private final String taskTitle;
    private final String dueDate;
    private final TaskStatus status;

    public TaskOccurence(String taskTitle, String dueDate, TaskStatus status) {
        this.taskTitle = taskTitle;
        this.dueDate = dueDate;
        this.status = status;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public String getDueDate() {
        return dueDate;
    }

    public TaskStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return taskTitle + " | " + dueDate + " | " + status;
    }
}
