package persistence;

import model.Priority;
import model.Task;
import model.TaskStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskRepository {

    public void addTask(Task task) {
        String sql = "INSERT INTO tasks(title, description, status, priority, dueDate) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setString(3, task.getStatus().name());
            ps.setString(4, task.getPriority().name());
            ps.setString(5, task.getDueDate());

            ps.executeUpdate();

        } catch (Exception e) {
            System.out.println("Could not add the task to the database.");
        }
    }

    public List<Task> getAllTasks() {
        List<Task> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM tasks")) {

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
        String sql = "SELECT * FROM tasks WHERE title LIKE ? OR description LIKE ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String k = "%" + keyword + "%";
            ps.setString(1, k);
            ps.setString(2, k);

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
        String sql = "SELECT * FROM tasks WHERE status = ?";

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
        String sql = "SELECT * FROM tasks WHERE priority = ?";

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
        String sql = "SELECT * FROM tasks WHERE dueDate = ?";

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
        String sql = "SELECT * FROM tasks WHERE dueDate BETWEEN ? AND ?";

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
                rs.getString("dueDate")
        );
    }
}