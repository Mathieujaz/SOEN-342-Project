package persistence;

import model.ActivityHistory;
import model.Collaborator;
import model.CollaboratorCategory;
import model.Priority;
import model.Project;
import model.RecurrencePattern;
import model.Subtask;
import model.Tag;
import model.Task;
import model.TaskStatus;
import service.TaskFilter;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TaskRepository {
    private static final String SELECT_ALL_COLUMNS =
            "SELECT t.id, t.title, t.description, t.status, t.priority, t.dueDate, " +
            "COALESCE(t.projectName, '') AS projectName, " +
            "COALESCE(p.description, '') AS projectDescription, " +
            "COALESCE(t.collaboratorName, '') AS collaboratorName, " +
            "COALESCE(t.collaboratorCategory, '') AS collaboratorCategory, " +
            "COALESCE(t.tags, '') AS tags, " +
            "COALESCE(t.subtasks, '') AS subtasks, " +
            "COALESCE(t.activityHistory, '') AS activityHistory, " +
            "COALESCE(t.recurrencePattern, '') AS recurrencePattern " +
            "FROM tasks t LEFT JOIN projects p ON t.projectName = p.name";

    public boolean createProject(Project project) {
        if (project == null || project.getName().isBlank()) {
            return false;
        }

        String sql = "INSERT OR REPLACE INTO projects(name, description) VALUES (?, COALESCE(NULLIF(?, ''), COALESCE((SELECT description FROM projects WHERE name = ?), '')))";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, project.getName());
            ps.setString(2, project.getDescription());
            ps.setString(3, project.getName());
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("Could not create the project.");
            return false;
        }
    }

    public List<Project> getAllProjects() {
        List<Project> projects = new ArrayList<>();
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name, description FROM projects ORDER BY name ASC")) {
            while (rs.next()) {
                projects.add(new Project(rs.getString("name"), rs.getString("description")));
            }
        } catch (Exception e) {
            System.out.println("Could not retrieve projects.");
        }
        return projects;
    }

    public Project getProjectByName(String projectName) {
        if (projectName == null || projectName.isBlank()) {
            return null;
        }
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT name, description FROM projects WHERE name = ?")) {
            ps.setString(1, projectName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Project(rs.getString("name"), rs.getString("description"));
            }
        } catch (Exception e) {
            System.out.println("Could not retrieve the project.");
        }
        return null;
    }

    public boolean addTask(Task task) {
        if (wouldExceedCollaboratorLimit(task)) {
            CollaboratorCategory category = task.getCollaboratorCategory();
            System.out.println("Could not assign task to " + task.getCollaboratorName()
                    + ": " + category + " collaborators are limited to " + category.getOpenTaskLimit() + " open tasks.");
            return false;
        }

        ensureProjectExists(task.getProjectName(), task.getProjectDescription());

        String sql = "INSERT INTO tasks(title, description, status, priority, dueDate, projectName, collaboratorName, collaboratorCategory, tags, subtasks, activityHistory, recurrencePattern) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindTask(ps, task);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("Could not add the task to the database.");
            return false;
        }
    }

    public boolean updateTask(Task task) {
        if (task == null) {
            return false;
        }
        if (wouldExceedCollaboratorLimitForUpdate(task)) {
            CollaboratorCategory category = task.getCollaboratorCategory();
            System.out.println("Could not assign task to " + task.getCollaboratorName()
                    + ": " + category + " collaborators are limited to " + category.getOpenTaskLimit() + " open tasks.");
            return false;
        }

        ensureProjectExists(task.getProjectName(), task.getProjectDescription());

        String sql = "UPDATE tasks SET title = ?, description = ?, status = ?, priority = ?, dueDate = ?, projectName = ?, collaboratorName = ?, collaboratorCategory = ?, tags = ?, subtasks = ?, activityHistory = ?, recurrencePattern = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindTask(ps, task);
            ps.setInt(13, task.getId());
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("Could not update the task.");
            return false;
        }
    }

    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_COLUMNS)) {
            while (rs.next()) {
                tasks.add(map(rs));
            }
        } catch (Exception e) {
            System.out.println("Could not retrieve tasks.");
        }
        return tasks;
    }

    public List<Task> searchTasks(String keyword) {
        String value = keyword == null ? "" : keyword.trim().toLowerCase();
        return getAllTasks().stream().filter(task -> matchesKeyword(task, value)).collect(Collectors.toList());
    }

    public List<Task> getTasksByStatus(String status) {
        if (status == null || status.isBlank()) return new ArrayList<>();
        return getAllTasks().stream()
                .filter(task -> task.getStatus().name().equalsIgnoreCase(status.trim()))
                .collect(Collectors.toList());
    }

    public List<Task> getTasksByPriority(String priority) {
        if (priority == null || priority.isBlank()) return new ArrayList<>();
        return getAllTasks().stream()
                .filter(task -> task.getPriority().name().equalsIgnoreCase(priority.trim()))
                .collect(Collectors.toList());
    }

    public List<Task> getTasksByDueDate(String dueDate) {
        return getAllTasks().stream()
                .filter(task -> task.getDueDate().equals(dueDate))
                .collect(Collectors.toList());
    }

    public List<Task> getTasksByDateRange(String startDate, String endDate) {
        return getAllTasks().stream()
                .filter(Task::hasDueDate)
                .filter(task -> task.getDueDate().compareTo(startDate) >= 0 && task.getDueDate().compareTo(endDate) <= 0)
                .collect(Collectors.toList());
    }

    public List<Task> getTasksByTag(String tagName) {
        String value = tagName == null ? "" : tagName.trim().toLowerCase();
        return getAllTasks().stream()
                .filter(task -> task.getTags().stream().anyMatch(tag -> tag.getName().equalsIgnoreCase(value)))
                .collect(Collectors.toList());
    }

    public Task getTaskById(int taskId) {
        String sql = SELECT_ALL_COLUMNS + " WHERE t.id = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return map(rs);
            }
        } catch (Exception e) {
            System.out.println("Could not retrieve the task.");
        }
        return null;
    }

    public List<Task> getTasksByProject(String projectName) {
        return getAllTasks().stream()
                .filter(task -> task.getProjectName().equalsIgnoreCase(projectName == null ? "" : projectName.trim()))
                .collect(Collectors.toList());
    }

    public List<Task> getTasksByFilter(TaskFilter filter) {
        return getAllTasks().stream().filter(filter::matches).collect(Collectors.toList());
    }

    public List<Collaborator> getOverloadedCollaborators() {
        List<Collaborator> collaborators = new ArrayList<>();
        String sql = """
                SELECT collaboratorName, collaboratorCategory, COUNT(*) AS openTaskCount
                FROM tasks
                WHERE collaboratorName IS NOT NULL
                  AND TRIM(collaboratorName) <> ''
                  AND collaboratorCategory IS NOT NULL
                  AND TRIM(collaboratorCategory) <> ''
                  AND status = 'OPEN'
                GROUP BY collaboratorName, collaboratorCategory
                ORDER BY openTaskCount DESC, collaboratorName ASC
                """;
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                CollaboratorCategory category = CollaboratorCategory.fromString(rs.getString("collaboratorCategory"));
                Collaborator collaborator = new Collaborator(rs.getString("collaboratorName"), category, rs.getLong("openTaskCount"));
                if (collaborator.isOverloaded()) {
                    collaborators.add(collaborator);
                }
            }
        } catch (Exception e) {
            System.out.println("Could not retrieve overloaded collaborators.");
        }
        return collaborators;
    }

    public void deleteAll() {
        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM tasks");
            stmt.executeUpdate("DELETE FROM projects");
        } catch (Exception e) {
            System.out.println("Could not clear the database.");
        }
    }

    private boolean matchesKeyword(Task task, String keyword) {
        if (keyword.isBlank()) {
            return true;
        }
        return lower(task.getTitle()).contains(keyword)
                || lower(task.getDescription()).contains(keyword)
                || lower(task.getProjectName()).contains(keyword)
                || lower(task.getProjectDescription()).contains(keyword)
                || lower(task.getCollaboratorName()).contains(keyword)
                || lower(task.getCollaboratorCategoryName()).contains(keyword)
                || task.getTags().stream().anyMatch(tag -> lower(tag.getName()).contains(keyword));
    }

    private void ensureProjectExists(String projectName, String projectDescription) {
        if (projectName == null || projectName.isBlank()) {
            return;
        }
        createProject(new Project(projectName, projectDescription));
    }

    private boolean wouldExceedCollaboratorLimit(Task task) {
        if (task.getStatus() != TaskStatus.OPEN || task.getCollaboratorName().isBlank() || task.getCollaboratorCategory() == null) {
            return false;
        }
        return countOpenTasksForCollaborator(task.getCollaboratorName(), task.getCollaboratorCategory(), null) >= task.getCollaboratorCategory().getOpenTaskLimit();
    }

    private boolean wouldExceedCollaboratorLimitForUpdate(Task task) {
        if (task.getStatus() != TaskStatus.OPEN || task.getCollaboratorName().isBlank() || task.getCollaboratorCategory() == null) {
            return false;
        }
        return countOpenTasksForCollaborator(task.getCollaboratorName(), task.getCollaboratorCategory(), task.getId()) >= task.getCollaboratorCategory().getOpenTaskLimit();
    }

    private long countOpenTasksForCollaborator(String collaboratorName, CollaboratorCategory category, Integer excludeTaskId) {
        String sql = "SELECT COUNT(*) FROM tasks WHERE collaboratorName = ? AND collaboratorCategory = ? AND status = 'OPEN'" + (excludeTaskId == null ? "" : " AND id <> ?");
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, collaboratorName);
            ps.setString(2, category.name());
            if (excludeTaskId != null) {
                ps.setInt(3, excludeTaskId);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (Exception e) {
            System.out.println("Could not count collaborator tasks.");
        }
        return 0;
    }

    private void bindTask(PreparedStatement ps, Task task) throws SQLException {
        ps.setString(1, task.getTitle());
        ps.setString(2, task.getDescription());
        ps.setString(3, task.getStatus().name());
        ps.setString(4, task.getPriority().name());
        ps.setString(5, task.getDueDate());
        ps.setString(6, task.getProjectName());
        ps.setString(7, task.getCollaboratorName());
        ps.setString(8, task.getCollaboratorCategoryName());
        ps.setString(9, serializeTags(task.getTags()));
        ps.setString(10, serializeSubtasks(task.getSubtasks()));
        ps.setString(11, serializeHistory(task.getActivityHistory()));
        ps.setString(12, serializeRecurrence(task.getRecurrencePattern()));
    }

    private Task map(ResultSet rs) throws SQLException {
        return new Task(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                TaskStatus.valueOf(rs.getString("status")),
                Priority.valueOf(rs.getString("priority")),
                rs.getString("dueDate"),
                rs.getString("projectName"),
                rs.getString("projectDescription"),
                rs.getString("collaboratorName"),
                CollaboratorCategory.fromString(rs.getString("collaboratorCategory")),
                parseSubtasks(rs.getString("subtasks")),
                parseTags(rs.getString("tags")),
                parseHistory(rs.getString("activityHistory")),
                parseRecurrence(rs.getString("recurrencePattern"))
        );
    }

    private String serializeTags(List<Tag> tags) {
        return tags.stream().map(Tag::getName).map(this::escapeValue).collect(Collectors.joining("||"));
    }

    private List<Tag> parseTags(String raw) {
        List<Tag> tags = new ArrayList<>();
        if (raw == null || raw.isBlank()) return tags;
        for (String item : raw.split("\\|\\|")) {
            tags.add(new Tag(unescapeValue(item)));
        }
        return tags;
    }

    private String serializeSubtasks(List<Subtask> subtasks) {
        return subtasks.stream()
                .map(s -> (s.isCompleted() ? "1" : "0") + "::" + escapeValue(s.getCollaboratorName()) + "::" + escapeValue(s.getTitle()))
                .collect(Collectors.joining("||"));
    }

    private List<Subtask> parseSubtasks(String raw) {
        List<Subtask> subtasks = new ArrayList<>();
        if (raw == null || raw.isBlank()) return subtasks;
        for (String item : raw.split("\\|\\|")) {
            String[] parts = item.split("::", 3);
            if (parts.length == 3) {
                subtasks.add(new Subtask(unescapeValue(parts[2]), "1".equals(parts[0]), unescapeValue(parts[1])));
            }
        }
        return subtasks;
    }

    private String serializeHistory(List<ActivityHistory> history) {
        return history.stream()
                .map(h -> escapeValue(h.getTimestamp()) + "::" + escapeValue(h.getDescription()))
                .collect(Collectors.joining("||"));
    }

    private List<ActivityHistory> parseHistory(String raw) {
        List<ActivityHistory> history = new ArrayList<>();
        if (raw == null || raw.isBlank()) return history;
        for (String item : raw.split("\\|\\|")) {
            String[] parts = item.split("::", 2);
            if (parts.length == 2) {
                history.add(new ActivityHistory(unescapeValue(parts[0]), unescapeValue(parts[1])));
            }
        }
        return history;
    }

    private String serializeRecurrence(RecurrencePattern recurrence) {
        if (recurrence == null || !recurrence.isRecurring()) {
            return "NONE";
        }
        String weekdays = recurrence.getWeekdays().stream().map(String::valueOf).collect(Collectors.joining(","));
        return recurrence.getType().name() + "::" + recurrence.getInterval() + "::" + escapeValue(recurrence.getStartDate()) + "::" + escapeValue(recurrence.getEndDate()) + "::" + weekdays + "::" + (recurrence.getDayOfMonth() == null ? "" : recurrence.getDayOfMonth());
    }

    private RecurrencePattern parseRecurrence(String raw) {
        if (raw == null || raw.isBlank() || raw.equals("NONE")) {
            return RecurrencePattern.none();
        }
        String[] parts = raw.split("::", 6);
        if (parts.length < 6) {
            return RecurrencePattern.none();
        }
        List<Integer> weekdays = new ArrayList<>();
        if (!parts[4].isBlank()) {
            weekdays = Arrays.stream(parts[4].split(",")).filter(s -> !s.isBlank()).map(Integer::parseInt).collect(Collectors.toList());
        }
        Integer dayOfMonth = parts[5].isBlank() ? null : Integer.parseInt(parts[5]);
        return new RecurrencePattern(RecurrencePattern.Type.valueOf(parts[0]), Integer.parseInt(parts[1]), unescapeValue(parts[2]), unescapeValue(parts[3]), weekdays, dayOfMonth);
    }

    private String escapeValue(String value) {
        return (value == null ? "" : value).replace("\\", "\\\\").replace("|", "\\|").replace(":", "\\:");
    }

    private String unescapeValue(String value) {
        return value.replace("\\:", ":").replace("\\|", "|").replace("\\\\", "\\");
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
