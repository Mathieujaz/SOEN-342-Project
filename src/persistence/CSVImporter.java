package persistence;

import model.CollaboratorCategory;
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
            int importedCount = 0;
            int skippedCount = 0;

            while ((line = br.readLine()) != null) {
                if (first) {
                    first = false;
                    continue;
                }

                List<String> values = parseCsvLine(line);

                if (values.size() < 5) {
                    System.out.println("A row in the CSV file is invalid and was skipped.");
                    skippedCount++;
                    continue;
                }

                String projectName = values.size() > 5 ? values.get(5).trim() : "";
                String collaboratorName = values.size() > 6 ? values.get(6).trim() : "";
                CollaboratorCategory collaboratorCategory = values.size() > 7
                        ? CollaboratorCategory.fromString(values.get(7).trim())
                        : null;
                List<Subtask> subtasks = values.size() > 8 ? parseSubtasks(values.get(8).trim()) : new ArrayList<>();

                Task t = new Task(
                        values.get(0).trim(),
                        values.get(1).trim(),
                        TaskStatus.valueOf(values.get(2).trim().toUpperCase()),
                        Priority.valueOf(values.get(3).trim().toUpperCase()),
                        values.get(4).trim(),
                        projectName,
                        collaboratorName,
                        collaboratorCategory,
                        subtasks
                );

                if (repo.addTask(t)) {
                    importedCount++;
                } else {
                    skippedCount++;
                }
            }

            System.out.println("Import completed successfully. Imported: " + importedCount + ", Skipped: " + skippedCount + ".");

        } catch (java.io.FileNotFoundException e) {
            System.out.println("File not found. Please enter a valid CSV file path.");
        } catch (IllegalArgumentException e) {
            System.out.println("The CSV file contains an invalid status, priority, or collaborator category value.");
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

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        values.add(current.toString());
        return values;
    }
}
