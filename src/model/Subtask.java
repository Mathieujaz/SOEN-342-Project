package model;

public class Subtask {
    private final String title;
    private final boolean completed;
    private final String collaboratorName;

    public Subtask(String title, boolean completed) {
        this(title, completed, "");
    }

    public Subtask(String title, boolean completed, String collaboratorName) {
        this.title = title;
        this.completed = completed;
        this.collaboratorName = collaboratorName == null ? "" : collaboratorName;
    }

    public String getTitle() {
        return title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getCollaboratorName() {
        return collaboratorName;
    }

    @Override
    public String toString() {
        String collaboratorPart = collaboratorName.isBlank() ? "" : " [" + collaboratorName + "]";
        return (completed ? "[x] " : "[ ] ") + title + collaboratorPart;
    }
}
