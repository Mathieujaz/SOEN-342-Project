package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Task {
    private int id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private String dueDate;
    private String projectName;
    private String collaboratorName;
    private CollaboratorCategory collaboratorCategory;
    private List<Subtask> subtasks;

    public Task(String title, String description, TaskStatus status, Priority priority, String dueDate) {
        this(0, title, description, status, priority, dueDate, "", "", null, new ArrayList<>());
    }

    public Task(int id, String title, String description, TaskStatus status, Priority priority, String dueDate) {
        this(id, title, description, status, priority, dueDate, "", "", null, new ArrayList<>());
    }

    public Task(String title, String description, TaskStatus status, Priority priority, String dueDate,
                String projectName, String collaboratorName, List<Subtask> subtasks) {
        this(0, title, description, status, priority, dueDate, projectName, collaboratorName, null, subtasks);
    }

    public Task(int id, String title, String description, TaskStatus status, Priority priority, String dueDate,
                String projectName, String collaboratorName, List<Subtask> subtasks) {
        this(id, title, description, status, priority, dueDate, projectName, collaboratorName, null, subtasks);
    }

    public Task(String title, String description, TaskStatus status, Priority priority, String dueDate,
                String projectName, String collaboratorName, CollaboratorCategory collaboratorCategory,
                List<Subtask> subtasks) {
        this(0, title, description, status, priority, dueDate, projectName, collaboratorName, collaboratorCategory, subtasks);
    }

    public Task(int id, String title, String description, TaskStatus status, Priority priority, String dueDate,
                String projectName, String collaboratorName, CollaboratorCategory collaboratorCategory,
                List<Subtask> subtasks) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.projectName = projectName == null ? "" : projectName;
        this.collaboratorName = collaboratorName == null ? "" : collaboratorName;
        this.collaboratorCategory = collaboratorCategory;
        this.subtasks = subtasks == null ? new ArrayList<>() : new ArrayList<>(subtasks);
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TaskStatus getStatus() { return status; }
    public Priority getPriority() { return priority; }
    public String getDueDate() { return dueDate; }
    public String getProjectName() { return projectName; }
    public String getCollaboratorName() { return collaboratorName; }
    public CollaboratorCategory getCollaboratorCategory() { return collaboratorCategory; }
    public String getCollaboratorCategoryName() { return collaboratorCategory == null ? "" : collaboratorCategory.name(); }
    public List<Subtask> getSubtasks() { return Collections.unmodifiableList(subtasks); }

    public boolean hasDueDate() {
        return dueDate != null && !dueDate.isBlank();
    }

    @Override
    public String toString() {
        String projectPart = projectName.isBlank() ? "No Project" : projectName;
        String collaboratorPart = collaboratorName.isBlank() ? "Unassigned" : collaboratorName;
        String categoryPart = collaboratorCategory == null ? "" : " (" + collaboratorCategory + ")";
        String dueDatePart = hasDueDate() ? dueDate : "No due date";
        return id + " | " + title + " | " + status + " | " + priority + " | " + dueDatePart
                + " | Project: " + projectPart + " | Collaborator: " + collaboratorPart + categoryPart;
    }
}
