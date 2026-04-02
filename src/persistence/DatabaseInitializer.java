package persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        String taskSql = """
            CREATE TABLE IF NOT EXISTS tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                description TEXT,
                status TEXT,
                priority TEXT,
                dueDate TEXT,
                projectName TEXT DEFAULT '',
                collaboratorName TEXT DEFAULT '',
                collaboratorCategory TEXT DEFAULT '',
                tags TEXT DEFAULT '',
                subtasks TEXT DEFAULT '',
                activityHistory TEXT DEFAULT '',
                recurrencePattern TEXT DEFAULT ''
            );
        """;

        String projectSql = """
            CREATE TABLE IF NOT EXISTS projects (
                name TEXT PRIMARY KEY,
                description TEXT DEFAULT ''
            );
        """;

        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(taskSql);
            stmt.execute(projectSql);
            addColumnIfMissing(conn, "projectName", "TEXT DEFAULT ''");
            addColumnIfMissing(conn, "collaboratorName", "TEXT DEFAULT ''");
            addColumnIfMissing(conn, "collaboratorCategory", "TEXT DEFAULT ''");
            addColumnIfMissing(conn, "tags", "TEXT DEFAULT ''");
            addColumnIfMissing(conn, "subtasks", "TEXT DEFAULT ''");
            addColumnIfMissing(conn, "activityHistory", "TEXT DEFAULT ''");
            addColumnIfMissing(conn, "recurrencePattern", "TEXT DEFAULT ''");
            System.out.println("Database ready.");

        } catch (Exception e) {
            System.out.println("Could not initialize the database.");
        }
    }

    private static void addColumnIfMissing(Connection conn, String columnName, String columnDefinition) throws Exception {
        try (Statement pragma = conn.createStatement();
             ResultSet rs = pragma.executeQuery("PRAGMA table_info(tasks)")) {
            while (rs.next()) {
                if (columnName.equalsIgnoreCase(rs.getString("name"))) {
                    return;
                }
            }
        }

        try (Statement alter = conn.createStatement()) {
            alter.execute("ALTER TABLE tasks ADD COLUMN " + columnName + " " + columnDefinition);
        }
    }
}
