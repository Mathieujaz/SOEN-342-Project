package persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        String sql = """
            CREATE TABLE IF NOT EXISTS tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                description TEXT,
                status TEXT,
                priority TEXT,
                dueDate TEXT,
                projectName TEXT DEFAULT '',
                collaboratorName TEXT DEFAULT '',
                subtasks TEXT DEFAULT ''
            );
        """;

        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            addColumnIfMissing(conn, "projectName", "TEXT DEFAULT ''");
            addColumnIfMissing(conn, "collaboratorName", "TEXT DEFAULT ''");
            addColumnIfMissing(conn, "subtasks", "TEXT DEFAULT ''");
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
