package persistence;

import model.Collaborator;
import model.Priority;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import service.TaskFilter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TaskRepository {
    private static final String SELECT_ALL_COLUMNS =
            "SELECT id, title, description, status, priority, dueDate, " +
            "COALESCE(projectName, '') AS projectName, " +
            "COALESCE(collaboratorName, '') AS collaboratorName, " +
            "COALESCE(subtasks, '') AS subtasks FROM tasks";

    public void addTask(Task task) {
        String sql = "INSERT INTO tasks(title, description, status, priority, dueDate, projectName, collaboratorName, subtasks) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getStatus().name());
            ps.setString(4, task.getPriority().name());
            ps.setString(5, task.getDueDate());
            ps.setString(6, task.getProjectName());
            ps.setString(7, task.getCollaboratorName());
            ps.setString(8, serializeSubtasks(task.getSubtasks()));

            ps.executeUpdate();

        } catch (Exception e) {
            System.out.println("Could not add the task to the database.");
        }
    }

    public List<Task> getAllTasks() {
        List<Task> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_COLUMNS)) {

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (Exception e) {
            System.out.println("Could not retrieve tasks.");
        }

        return list;
    }

    public List<Task> searchTasks(String keyword) {
        List<Task> list = new ArrayList<>();
        String sql = SELECT_ALL_COLUMNS + " WHERE title LIKE ? OR description LIKE ? OR projectName LIKE ? OR collaboratorName LIKE ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String k = "%" + keyword + "%";
            ps.setString(1, k);
            ps.setString(2, k);
            ps.setString(3, k);
            ps.setString(4, k);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (Exception e) {
            System.out.println("Could not search tasks.");
        }

        return list;
    }

    public List<Task> getTasksByStatus(String status) {
        List<Task> list = new ArrayList<>();
        String sql = SELECT_ALL_COLUMNS + " WHERE status = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.toUpperCase());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (Exception e) {
            System.out.println("Could not retrieve tasks by status.");
        }

        return list;
    }

    public List<Task> getTasksByPriority(String priority) {
        List<Task> list = new ArrayList<>();
        String sql = SELECT_ALL_COLUMNS + " WHERE priority = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, priority.toUpperCase());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (Exception e) {
            System.out.println("Could not retrieve tasks by priority.");
        }

        return list;
    }

    public List<Task> getTasksByDueDate(String dueDate) {
        List<Task> list = new ArrayList<>();
        String sql = SELECT_ALL_COLUMNS + " WHERE dueDate = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dueDate);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (Exception e) {
            System.out.println("Could not retrieve tasks by due date.");
        }

        return list;
    }

    public List<Task> getTasksByDateRange(String startDate, String endDate) {
        List<Task> list = new ArrayList<>();
        String sql = SELECT_ALL_COLUMNS + " WHERE dueDate BETWEEN ? AND ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, startDate);
            ps.setString(2, endDate);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(map(rs));
            }

        } catch (Exception e) {
            System.out.println("Could not retrieve tasks by date range.");
        }

        return list;
    }

    public Task getTaskById(int taskId) {
        String sql = SELECT_ALL_COLUMNS + " WHERE id = ?";

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
        List<Task> list = new ArrayList<>();
        String sql = SELECT_ALL_COLUMNS + " WHERE projectName = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, projectName);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (Exception e) {
            System.out.println("Could not retrieve tasks by project.");
        }

        return list;
    }

    public List<Task> getTasksByFilter(TaskFilter filter) {
        return getAllTasks().stream()
                .filter(filter::matches)
                .collect(Collectors.toList());
    }

    public List<Collaborator> getOverloadedCollaborators(int openTaskThreshold) {
        List<Collaborator> collaborators = new ArrayList<>();
        String sql = """
                SELECT collaboratorName, COUNT(*) AS openTaskCount
                FROM tasks
                WHERE collaboratorName IS NOT NULL
                  AND TRIM(collaboratorName) <> ''
                  AND status = 'OPEN'
                GROUP BY collaboratorName
                HAVING COUNT(*) > ?
                ORDER BY openTaskCount DESC, collaboratorName ASC
                """;

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, openTaskThreshold);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                collaborators.add(new Collaborator(
                        rs.getString("collaboratorName"),
                        rs.getLong("openTaskCount")
                ));
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

        } catch (Exception e) {
            System.out.println("Could not clear the database.");
        }
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
                rs.getString("collaboratorName"),
                parseSubtasks(rs.getString("subtasks"))
        );
    }

    private String serializeSubtasks(List<Subtask> subtasks) {
        return subtasks.stream()
                .map(subtask -> (subtask.isCompleted() ? "1" : "0") + "::" + escapeSubtaskValue(subtask.getTitle()))
                .collect(Collectors.joining("||"));
    }

    private List<Subtask> parseSubtasks(String rawValue) {
        List<Subtask> subtasks = new ArrayList<>();
        if (rawValue == null || rawValue.isBlank()) {
            return subtasks;
        }

        String[] items = rawValue.split("\\|\\|");
        for (String item : items) {
            String[] parts = item.split("::", 2);
            if (parts.length == 2) {
                subtasks.add(new Subtask(unescapeSubtaskValue(parts[1]), "1".equals(parts[0])));
            }
        }
        return subtasks;
    }

    private String escapeSubtaskValue(String value) {
        return value.replace("\\", "\\\\").replace("|", "\\|").replace(":", "\\:");
    }

    private String unescapeSubtaskValue(String value) {
        return value.replace("\\:", ":").replace("\\|", "|").replace("\\\\", "\\");
    }
}
