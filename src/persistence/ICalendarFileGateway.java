package persistence;

import model.Subtask;
import model.Task;
import service.ICalendarGateway;

import java.io.FileWriter;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class ICalendarFileGateway implements ICalendarGateway {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    @Override
    public void exportTasks(List<Task> tasks, String path) {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write("BEGIN:VCALENDAR\r\n");
            writer.write("VERSION:2.0\r\n");
            writer.write("PRODID:-//SOEN342//Task Management System//EN\r\n");
            writer.write("CALSCALE:GREGORIAN\r\n");

            for (Task task : tasks) {
                if (!task.hasDueDate()) {
                    continue;
                }

                writer.write("BEGIN:VEVENT\r\n");
                writer.write("UID:" + escape(UUID.randomUUID() + "@soen342-project") + "\r\n");
                writer.write("DTSTAMP:" + ZonedDateTime.now(ZoneOffset.UTC).format(TIMESTAMP_FORMAT) + "\r\n");
                writer.write("DTSTART;VALUE=DATE:" + formatDate(task.getDueDate()) + "\r\n");
                writer.write("DUE;VALUE=DATE:" + formatDate(task.getDueDate()) + "\r\n");
                writer.write("SUMMARY:" + escape(task.getTitle()) + "\r\n");
                writer.write("DESCRIPTION:" + escape(buildDescription(task)) + "\r\n");
                writer.write("STATUS:" + mapStatus(task) + "\r\n");
                writer.write("PRIORITY:" + mapPriority(task) + "\r\n");
                writer.write("END:VEVENT\r\n");
            }

            writer.write("END:VCALENDAR\r\n");
            System.out.println("iCal export completed successfully.");
        } catch (Exception e) {
            System.out.println("Could not export the tasks to iCal.");
        }
    }

    private String buildDescription(Task task) {
        StringBuilder builder = new StringBuilder();
        builder.append("Description: ").append(task.getDescription() == null ? "" : task.getDescription());
        builder.append("\nStatus: ").append(task.getStatus());
        builder.append("\nPriority: ").append(task.getPriority());
        builder.append("\nDue Date: ").append(task.getDueDate());
        builder.append("\nProject: ").append(task.getProjectName().isBlank() ? "None" : task.getProjectName());

        if (!task.getSubtasks().isEmpty()) {
            builder.append("\nSubtasks:");
            for (Subtask subtask : task.getSubtasks()) {
                builder.append("\n- ").append(subtask);
            }
        }

        return builder.toString();
    }

    private String mapStatus(Task task) {
        return switch (task.getStatus()) {
            case COMPLETED -> "COMPLETED";
            case CANCELLED -> "CANCELLED";
            default -> "CONFIRMED";
        };
    }

    private int mapPriority(Task task) {
        return switch (task.getPriority()) {
            case HIGH -> 1;
            case MEDIUM -> 5;
            default -> 9;
        };
    }

    private String formatDate(String dueDate) {
        return LocalDate.parse(dueDate).format(DATE_FORMAT);
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r", "")
                .replace("\n", "\\n");
    }
}
