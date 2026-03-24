package persistence;

import model.Task;

import java.io.FileWriter;
import java.util.List;

public class CSVExporter {

    private TaskRepository repo;

    public CSVExporter(TaskRepository repo) {
        this.repo = repo;
    }

    public void exportCSV(String path) {
        try (FileWriter w = new FileWriter(path)) {

            w.write("title,description,status,priority,dueDate\n");

            List<Task> tasks = repo.getAllTasks();

            for (Task t : tasks) {
                w.write(t.getTitle() + "," +
                        t.getDescription() + "," +
                        t.getStatus() + "," +
                        t.getPriority() + "," +
                        t.getDueDate() + "\n");
            }

            System.out.println("Export completed successfully.");

        } catch (Exception e) {
            System.out.println("Could not export the tasks to CSV.");
        }
    }
}