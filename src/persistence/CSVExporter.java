package persistence;

import model.Subtask;
import model.Task;

import java.io.FileWriter;
import java.util.List;
import java.util.stream.Collectors;

public class CSVExporter {

    private TaskRepository repo;

    public CSVExporter(TaskRepository repo) {
        this.repo = repo;
    }

    public void exportCSV(String path) {
        try (FileWriter w = new FileWriter(path)) {

            w.write("title,description,status,priority,dueDate,projectName,collaboratorName,subtasks\n");

            List<Task> tasks = repo.getAllTasks();

            for (Task t : tasks) {
                w.write(csvValue(t.getTitle()) + "," +
                        csvValue(t.getDescription()) + "," +
                        csvValue(t.getStatus().toString()) + "," +
                        csvValue(t.getPriority().toString()) + "," +
                        csvValue(t.getDueDate()) + "," +
                        csvValue(t.getProjectName()) + "," +
                        csvValue(t.getCollaboratorName()) + "," +
                        csvValue(formatSubtasks(t.getSubtasks())) + "\n");
            }

            System.out.println("Export completed successfully.");

        } catch (Exception e) {
            System.out.println("Could not export the tasks to CSV.");
        }
    }

    private String formatSubtasks(List<Subtask> subtasks) {
        return subtasks.stream()
                .map(Subtask::toString)
                .collect(Collectors.joining("; "));
    }

    private String csvValue(String value) {
        String safeValue = value == null ? "" : value;
        return "\"" + safeValue.replace("\"", "\"\"") + "\"";
    }
}
