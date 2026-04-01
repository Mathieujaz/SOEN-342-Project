package service;

import model.Priority;
import model.Task;
import model.TaskStatus;

import java.time.LocalDate;

public class TaskFilter {
    private String keyword;
    private TaskStatus status;
    private Priority priority;
    private LocalDate startDate;
    private LocalDate endDate;

    public boolean matches(Task task) {
        if (keyword != null && !keyword.isBlank()) {
            String value = keyword.toLowerCase();
            String title = task.getTitle() == null ? "" : task.getTitle().toLowerCase();
            String description = task.getDescription() == null ? "" : task.getDescription().toLowerCase();
            if (!title.contains(value) && !description.contains(value)) {
                return false;
            }
        }

        if (status != null && task.getStatus() != status) {
            return false;
        }

        if (priority != null && task.getPriority() != priority) {
            return false;
        }

        if (startDate != null || endDate != null) {
            if (!task.hasDueDate()) {
                return false;
            }

            LocalDate dueDate = LocalDate.parse(task.getDueDate());
            if (startDate != null && dueDate.isBefore(startDate)) {
                return false;
            }
            if (endDate != null && dueDate.isAfter(endDate)) {
                return false;
            }
        }

        return true;
    }

    public TaskFilter withKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public TaskFilter withStatus(TaskStatus status) {
        this.status = status;
        return this;
    }

    public TaskFilter withPriority(Priority priority) {
        this.priority = priority;
        return this;
    }

    public TaskFilter withStartDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public TaskFilter withEndDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }
}
