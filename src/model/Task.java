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
    private String projectDescription;
    private String collaboratorName;
    private CollaboratorCategory collaboratorCategory;
    private List<Subtask> subtasks;
    private List<Tag> tags;
    private List<ActivityHistory> activityHistory;
    private RecurrencePattern recurrencePattern;

    public Task(String title, String description, TaskStatus status, Priority priority, String dueDate) {
        this(0, title, description, status, priority, dueDate, "", "", "", null,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), RecurrencePattern.none());
    }

    public Task(int id, String title, String description, TaskStatus status, Priority priority, String dueDate,
                String projectName, String collaboratorName, List<Subtask> subtasks) {
        this(id, title, description, status, priority, dueDate, projectName, "", collaboratorName, null,
                subtasks, new ArrayList<>(), new ArrayList<>(), RecurrencePattern.none());
    }

    public Task(String title, String description, TaskStatus status, Priority priority, String dueDate,
                String projectName, String collaboratorName, CollaboratorCategory collaboratorCategory,
                List<Subtask> subtasks) {
        this(0, title, description, status, priority, dueDate, projectName, "", collaboratorName, collaboratorCategory,
                subtasks, new ArrayList<>(), new ArrayList<>(), RecurrencePattern.none());
    }

    public Task(int id, String title, String description, TaskStatus status, Priority priority, String dueDate,
                String projectName, String projectDescription, String collaboratorName, CollaboratorCategory collaboratorCategory,
                List<Subtask> subtasks, List<Tag> tags, List<ActivityHistory> activityHistory, RecurrencePattern recurrencePattern) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate == null ? "" : dueDate;
        this.projectName = projectName == null ? "" : projectName;
        this.projectDescription = projectDescription == null ? "" : projectDescription;
        this.collaboratorName = collaboratorName == null ? "" : collaboratorName;
        this.collaboratorCategory = collaboratorCategory;
        this.subtasks = subtasks == null ? new ArrayList<>() : new ArrayList<>(subtasks);
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
        this.activityHistory = activityHistory == null ? new ArrayList<>() : new ArrayList<>(activityHistory);
        this.recurrencePattern = recurrencePattern == null ? RecurrencePattern.none() : recurrencePattern;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TaskStatus getStatus() { return status; }
    public Priority getPriority() { return priority; }
    public String getDueDate() { return dueDate; }
    public String getProjectName() { return projectName; }
    public String getProjectDescription() { return projectDescription; }
    public String getCollaboratorName() { return collaboratorName; }
    public CollaboratorCategory getCollaboratorCategory() { return collaboratorCategory; }
    public String getCollaboratorCategoryName() { return collaboratorCategory == null ? "" : collaboratorCategory.name(); }
    public List<Subtask> getSubtasks() { return Collections.unmodifiableList(subtasks); }
    public List<Tag> getTags() { return Collections.unmodifiableList(tags); }
    public List<ActivityHistory> getActivityHistory() { return Collections.unmodifiableList(activityHistory); }
    public RecurrencePattern getRecurrencePattern() { return recurrencePattern; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate == null ? "" : dueDate; }
    public void setProject(String projectName, String projectDescription) {
        this.projectName = projectName == null ? "" : projectName;
        this.projectDescription = projectDescription == null ? "" : projectDescription;
    }
    public void setCollaborator(String collaboratorName, CollaboratorCategory collaboratorCategory) {
        this.collaboratorName = collaboratorName == null ? "" : collaboratorName;
        this.collaboratorCategory = collaboratorCategory;
    }
    public void setRecurrencePattern(RecurrencePattern recurrencePattern) {
        this.recurrencePattern = recurrencePattern == null ? RecurrencePattern.none() : recurrencePattern;
    }

    public void addTag(Tag tag) {
        if (tag != null && !tag.getName().isBlank()) {
            tags.add(tag);
        }
    }

    public void addSubtask(Subtask subtask) {
        if (subtask != null) {
            subtasks.add(subtask);
        }
    }

    public void addActivity(ActivityHistory activity) {
        if (activity != null) {
            activityHistory.add(activity);
        }
    }

    public boolean hasDueDate() {
        return dueDate != null && !dueDate.isBlank();
    }

    @Override
    public String toString() {
        String projectPart = projectName.isBlank() ? "No Project" : projectName;
        String collaboratorPart = collaboratorName.isBlank() ? "Unassigned" : collaboratorName;
        String categoryPart = collaboratorCategory == null ? "" : " (" + collaboratorCategory + ")";
        String dueDatePart = hasDueDate() ? dueDate : "No due date";
        String tagPart = tags.isEmpty() ? "" : " | Tags: " + tags;
        return id + " | " + title + " | " + status + " | " + priority + " | " + dueDatePart
                + " | Project: " + projectPart + " | Collaborator: " + collaboratorPart + categoryPart + tagPart;
    }
}
