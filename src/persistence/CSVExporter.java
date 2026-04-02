package persistence;

import model.ActivityHistory;
import model.Subtask;
import model.Tag;
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

            w.write("title,description,status,priority,dueDate,projectName,projectDescription,collaboratorName,collaboratorCategory,tags,subtasks,recurrencePattern\n");

            List<Task> tasks = repo.getAllTasks();

            for (Task t : tasks) {
                w.write(csvValue(t.getTitle()) + "," +
                        csvValue(t.getDescription()) + "," +
                        csvValue(t.getStatus().toString()) + "," +
                        csvValue(t.getPriority().toString()) + "," +
                        csvValue(t.getDueDate()) + "," +
                        csvValue(t.getProjectName()) + "," +
                        csvValue(t.getProjectDescription()) + "," +
                        csvValue(t.getCollaboratorName()) + "," +
                        csvValue(t.getCollaboratorCategoryName()) + "," +
                        csvValue(formatTags(t.getTags())) + "," +
                        csvValue(formatSubtasks(t.getSubtasks())) + "," +
                        csvValue(formatRecurrence(t)) + "\n");
            }

            System.out.println("Export completed successfully.");

        } catch (Exception e) {
            System.out.println("Could not export the tasks to CSV.");
        }
    }

    private String formatTags(List<Tag> tags) {
        return tags.stream().map(Tag::getName).collect(Collectors.joining("; "));
    }

    private String formatSubtasks(List<Subtask> subtasks) {
        return subtasks.stream().map(Subtask::toString).collect(Collectors.joining("; "));
    }

    private String formatRecurrence(Task task) {
        if (task.getRecurrencePattern() == null || !task.getRecurrencePattern().isRecurring()) {
            return "NONE";
        }
        String weekdays = task.getRecurrencePattern().getWeekdays().stream().map(String::valueOf).collect(Collectors.joining(","));
        String dayOfMonth = task.getRecurrencePattern().getDayOfMonth() == null ? "" : String.valueOf(task.getRecurrencePattern().getDayOfMonth());
        return task.getRecurrencePattern().getType() + "|" + task.getRecurrencePattern().getInterval() + "|"
                + task.getRecurrencePattern().getStartDate() + "|" + task.getRecurrencePattern().getEndDate() + "|"
                + weekdays + "|" + dayOfMonth;
    }

    private String csvValue(String value) {
        String safeValue = value == null ? "" : value;
        return "\"" + safeValue.replace("\"", "\"\"") + "\"";
    }
}
