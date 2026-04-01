package persistence;

import model.Priority;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CSVImporter {

    private TaskRepository repo;

    public CSVImporter(TaskRepository repo) {
        this.repo = repo;
    }

    public void importCSV(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {
                if (first) {
                    first = false;
                    continue;
                }

                String[] v = line.split(",");

                if (v.length < 5) {
                    System.out.println("A row in the CSV file is invalid and was skipped.");
                    continue;
                }

                String projectName = v.length > 5 ? v[5].trim() : "";
                String collaboratorName = v.length > 6 ? v[6].trim() : "";
                List<Subtask> subtasks = v.length > 7 ? parseSubtasks(v[7].trim()) : new ArrayList<>();

                Task t = new Task(
                        v[0].trim(),
                        v[1].trim(),
                        TaskStatus.valueOf(v[2].trim().toUpperCase()),
                        Priority.valueOf(v[3].trim().toUpperCase()),
                        v[4].trim(),
                        projectName,
                        collaboratorName,
                        subtasks
                );

                repo.addTask(t);
            }

            System.out.println("Import completed successfully.");

        } catch (java.io.FileNotFoundException e) {
            System.out.println("File not found. Please enter a valid CSV file path.");
        } catch (IllegalArgumentException e) {
            System.out.println("The CSV file contains an invalid status or priority value.");
        } catch (Exception e) {
            System.out.println("Could not import the CSV file.");
        }
    }

    private List<Subtask> parseSubtasks(String rawValue) {
        List<Subtask> subtasks = new ArrayList<>();
        if (rawValue == null || rawValue.isBlank()) {
            return subtasks;
        }

        String[] values = rawValue.split(";");
        for (String value : values) {
            String trimmed = value.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            boolean completed = trimmed.startsWith("[x]") || trimmed.startsWith("[X]");
            String title = trimmed.replaceFirst("^\\[(x|X| )\\]\\s*", "").trim();
            subtasks.add(new Subtask(title.isEmpty() ? trimmed : title, completed));
        }

        return subtasks;
    }
}
