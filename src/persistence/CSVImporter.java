package persistence;

import model.CollaboratorCategory;
import model.Priority;
import model.RecurrencePattern;
import model.Subtask;
import model.Tag;
import model.Task;
import model.TaskStatus;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            Map<String, Integer> headerMap = new HashMap<>();

            while ((line = br.readLine()) != null) {
                List<String> values = parseCsvLine(line);
                if (first) {
                    first = false;
                    headerMap = buildHeaderMap(values);
                    continue;
                }

                if (values.size() < 5) {
                    System.out.println("A row in the CSV file is invalid and was skipped.");
                    skippedCount++;
                    continue;
                }

                String title = getValue(values, headerMap, "title", 0);
                String description = getValue(values, headerMap, "description", 1);
                String statusValue = getValue(values, headerMap, "status", 2);
                String priorityValue = getValue(values, headerMap, "priority", 3);
                String dueDate = getValue(values, headerMap, "duedate", 4);
                String projectName = getValue(values, headerMap, "projectname", 5);
                String projectDescription = getValue(values, headerMap, "projectdescription", 6);
                String collaboratorName = getValue(values, headerMap, "collaboratorname", 7);
                String collaboratorCategoryValue = getValue(values, headerMap, "collaboratorcategory", 8);
                String tagsValue = getValue(values, headerMap, "tags", 9);
                String subtasksValue = getValue(values, headerMap, "subtasks", 10);
                String recurrenceValue = getValue(values, headerMap, "recurrencepattern", 11);

                Task t = new Task(
                        0,
                        title.trim(),
                        description.trim(),
                        TaskStatus.valueOf(statusValue.trim().toUpperCase()),
                        Priority.valueOf(priorityValue.trim().toUpperCase()),
                        dueDate.trim(),
                        projectName.trim(),
                        projectDescription.trim(),
                        collaboratorName.trim(),
                        CollaboratorCategory.fromString(collaboratorCategoryValue.trim()),
                        parseSubtasks(subtasksValue),
                        parseTags(tagsValue),
                        new ArrayList<>(),
                        parseRecurrence(recurrenceValue)
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

    private Map<String, Integer> buildHeaderMap(List<String> headers) {
        Map<String, Integer> headerMap = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            headerMap.put(headers.get(i).trim().toLowerCase(), i);
        }
        return headerMap;
    }

    private String getValue(List<String> values, Map<String, Integer> headerMap, String key, int fallbackIndex) {
        Integer index = headerMap.get(key);
        if (index != null && index < values.size()) {
            return values.get(index);
        }
        return fallbackIndex < values.size() ? values.get(fallbackIndex) : "";
    }

    private List<Tag> parseTags(String rawValue) {
        List<Tag> tags = new ArrayList<>();
        if (rawValue == null || rawValue.isBlank()) {
            return tags;
        }
        for (String value : rawValue.split(";")) {
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                tags.add(new Tag(trimmed));
            }
        }
        return tags;
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
            String content = trimmed.replaceFirst("^\\[(x|X| )\\]\\s*", "").trim();
            String collaboratorName = "";
            String title = content;
            int collaboratorStart = content.lastIndexOf('[');
            int collaboratorEnd = content.endsWith("]") ? content.length() - 1 : -1;
            if (collaboratorStart > 0 && collaboratorEnd > collaboratorStart) {
                collaboratorName = content.substring(collaboratorStart + 1, collaboratorEnd).trim();
                title = content.substring(0, collaboratorStart).trim();
            }
            subtasks.add(new Subtask(title.isEmpty() ? trimmed : title, completed, collaboratorName));
        }

        return subtasks;
    }

    private RecurrencePattern parseRecurrence(String rawValue) {
        if (rawValue == null || rawValue.isBlank() || rawValue.equalsIgnoreCase("NONE")) {
            return RecurrencePattern.none();
        }
        String[] parts = rawValue.split("\\|");
        try {
            RecurrencePattern.Type type = RecurrencePattern.Type.valueOf(parts[0].trim().toUpperCase());
            int interval = parts.length > 1 && !parts[1].isBlank() ? Integer.parseInt(parts[1].trim()) : 1;
            String startDate = parts.length > 2 ? parts[2].trim() : "";
            String endDate = parts.length > 3 ? parts[3].trim() : "";
            List<Integer> weekdays = new ArrayList<>();
            if (parts.length > 4 && !parts[4].isBlank()) {
                weekdays = java.util.Arrays.stream(parts[4].split(",")).map(String::trim).filter(s -> !s.isBlank()).map(Integer::parseInt).collect(Collectors.toList());
            }
            Integer dayOfMonth = parts.length > 5 && !parts[5].isBlank() ? Integer.parseInt(parts[5].trim()) : null;
            return new RecurrencePattern(type, interval, startDate, endDate, weekdays, dayOfMonth);
        } catch (Exception e) {
            return RecurrencePattern.none();
        }
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
