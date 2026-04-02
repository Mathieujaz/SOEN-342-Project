package service;

import model.ActivityHistory;
import model.Collaborator;
import model.CollaboratorCategory;
import model.Priority;
import model.Project;
import model.RecurrencePattern;
import model.Subtask;
import model.Tag;
import model.Task;
import model.TaskOccurence;
import model.TaskStatus;
import persistence.CSVExporter;
import persistence.CSVImporter;
import persistence.ICalendarFileGateway;
import persistence.TaskRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TaskService {

    private TaskRepository repo = new TaskRepository();
    private CSVImporter importer = new CSVImporter(repo);
    private CSVExporter exporter = new CSVExporter(repo);
    private ICalendarGateway calendarGateway = new ICalendarFileGateway();

    public void importCSV(String path) {
        importer.importCSV(path);
    }

    public boolean createProject(String name, String description) {
        return repo.createProject(new Project(name, description));
    }

    public List<Project> getAllProjects() {
        return repo.getAllProjects();
    }

    public boolean createTask(String title, String description, String status, String priority, String dueDate,
                              String projectName, String projectDescription) {
        Task task = new Task(title, description, TaskStatus.valueOf(status.toUpperCase()), Priority.valueOf(priority.toUpperCase()), dueDate);
        task.setProject(projectName, projectDescription);
        task.addActivity(new ActivityHistory(now(), "Task created"));
        return repo.addTask(task);
    }

    public boolean updateTaskBasics(int taskId, String title, String description, String status, String priority, String dueDate) {
        Task task = repo.getTaskById(taskId);
        if (task == null) {
            return false;
        }
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(TaskStatus.valueOf(status.toUpperCase()));
        task.setPriority(Priority.valueOf(priority.toUpperCase()));
        task.setDueDate(dueDate);
        task.addActivity(new ActivityHistory(now(), "Task updated"));
        return repo.updateTask(task);
    }

    public boolean moveTaskToProject(int taskId, String projectName, String projectDescription) {
        Task task = repo.getTaskById(taskId);
        if (task == null) {
            return false;
        }
        task.setProject(projectName, projectDescription);
        task.addActivity(new ActivityHistory(now(), projectName.isBlank() ? "Task removed from project" : "Task moved to project " + projectName));
        return repo.updateTask(task);
    }

    public boolean addTagToTask(int taskId, String tagName) {
        Task task = repo.getTaskById(taskId);
        if (task == null) return false;
        task.addTag(new Tag(tagName));
        task.addActivity(new ActivityHistory(now(), "Tag added: " + tagName));
        return repo.updateTask(task);
    }

    public boolean addSubtaskToTask(int taskId, String subtaskTitle) {
        Task task = repo.getTaskById(taskId);
        if (task == null) return false;
        task.addSubtask(new Subtask(subtaskTitle, false));
        task.addActivity(new ActivityHistory(now(), "Subtask added: " + subtaskTitle));
        return repo.updateTask(task);
    }

    public boolean assignCollaboratorToTask(int taskId, String collaboratorName, String categoryName) {
        Task task = repo.getTaskById(taskId);
        if (task == null) return false;
        CollaboratorCategory category = CollaboratorCategory.fromString(categoryName);
        task.setCollaborator(collaboratorName, category);
        task.addSubtask(new Subtask("Collaborator task", false, collaboratorName));
        task.addActivity(new ActivityHistory(now(), "Collaborator assigned: " + collaboratorName + " (" + category + ")"));
        return repo.updateTask(task);
    }

    public List<ActivityHistory> getTaskActivityHistory(int taskId) {
        Task task = repo.getTaskById(taskId);
        return task == null ? new ArrayList<>() : task.getActivityHistory();
    }

    public boolean setTaskRecurrence(int taskId, RecurrencePattern recurrencePattern) {
        Task task = repo.getTaskById(taskId);
        if (task == null) return false;
        task.setRecurrencePattern(recurrencePattern);
        task.addActivity(new ActivityHistory(now(), recurrencePattern.isRecurring() ? "Recurrence updated" : "Recurrence removed"));
        return repo.updateTask(task);
    }

    public List<TaskOccurence> getTaskOccurrences(int taskId, int maxCount) {
        Task task = repo.getTaskById(taskId);
        if (task == null) {
            return new ArrayList<>();
        }
        return generateOccurrences(task, maxCount <= 0 ? 10 : maxCount);
    }

    public List<Task> search(String keyword) {
        return repo.searchTasks(keyword);
    }

    public List<Task> searchByStatus(String status) {
        return repo.getTasksByStatus(status);
    }

    public List<Task> searchByPriority(String priority) {
        return repo.getTasksByPriority(priority);
    }

    public List<Task> searchByDate(String dueDate) {
        return repo.getTasksByDueDate(dueDate);
    }

    public List<Task> searchByDateRange(String startDate, String endDate) {
        return repo.getTasksByDateRange(startDate, endDate);
    }

    public List<Task> searchByProject(String projectName) {
        return repo.getTasksByProject(projectName);
    }

    public List<Task> searchByTag(String tagName) {
        return repo.getTasksByTag(tagName);
    }

    public List<Task> getAll() {
        return repo.getAllTasks();
    }

    public Task getTaskById(int taskId) {
        return repo.getTaskById(taskId);
    }

    public void exportCSV(String path) {
        exporter.exportCSV(path);
    }

    public void exportSingleTaskToICal(int taskId, String path) {
        Task task = repo.getTaskById(taskId);
        if (task == null) {
            System.out.println("Task not found.");
            return;
        }
        exportEligibleTasksToICal(List.of(task), path);
    }

    public void exportProjectTasksToICal(String projectName, String path) {
        exportEligibleTasksToICal(repo.getTasksByProject(projectName), path);
    }

    public void exportFilteredTasksToICal(TaskFilter filter, String path) {
        exportEligibleTasksToICal(repo.getTasksByFilter(filter), path);
    }

    public List<Collaborator> getOverloadedCollaborators() {
        return repo.getOverloadedCollaborators();
    }

    public void updateCollaboratorLimit(String categoryName, int limit) {
        CollaboratorCategory category = CollaboratorCategory.fromString(categoryName);
        if (category == null) throw new IllegalArgumentException("Invalid collaborator category.");
        category.setOpenTaskLimit(limit);
    }

    public void resetCollaboratorLimits() {
        for (CollaboratorCategory category : CollaboratorCategory.values()) {
            category.resetOpenTaskLimit();
        }
    }

    public String getCollaboratorLimitsSummary() {
        return "SENIOR=" + CollaboratorCategory.SENIOR.getOpenTaskLimit()
                + ", INTERMEDIATE=" + CollaboratorCategory.INTERMEDIATE.getOpenTaskLimit()
                + ", JUNIOR=" + CollaboratorCategory.JUNIOR.getOpenTaskLimit();
    }

    public void reset() {
        repo.deleteAll();
    }

    public TaskFilter buildFilter(String keyword, String status, String priority, String startDate, String endDate) {
        TaskFilter filter = new TaskFilter();
        if (keyword != null && !keyword.isBlank()) filter.withKeyword(keyword.trim());
        if (status != null && !status.isBlank()) filter.withStatus(TaskStatus.valueOf(status.trim().toUpperCase()));
        if (priority != null && !priority.isBlank()) filter.withPriority(Priority.valueOf(priority.trim().toUpperCase()));
        if (startDate != null && !startDate.isBlank()) filter.withStartDate(LocalDate.parse(startDate.trim()));
        if (endDate != null && !endDate.isBlank()) filter.withEndDate(LocalDate.parse(endDate.trim()));
        return filter;
    }

    private List<TaskOccurence> generateOccurrences(Task task, int maxCount) {
        List<TaskOccurence> occurrences = new ArrayList<>();
        if (!task.getRecurrencePattern().isRecurring()) {
            if (task.hasDueDate()) {
                occurrences.add(new TaskOccurence(task.getTitle(), task.getDueDate(), task.getStatus()));
            }
            return occurrences;
        }

        RecurrencePattern recurrence = task.getRecurrencePattern();
        LocalDate start = LocalDate.parse(recurrence.getStartDate().isBlank() ? task.getDueDate() : recurrence.getStartDate());
        LocalDate end = recurrence.getEndDate().isBlank() ? start.plusMonths(3) : LocalDate.parse(recurrence.getEndDate());
        LocalDate current = start;

        while (!current.isAfter(end) && occurrences.size() < maxCount) {
            switch (recurrence.getType()) {
                case DAILY -> {
                    occurrences.add(new TaskOccurence(task.getTitle(), current.toString(), task.getStatus()));
                    current = current.plusDays(recurrence.getInterval());
                }
                case WEEKLY -> {
                    if (recurrence.getWeekdays().isEmpty() || recurrence.getWeekdays().contains(current.getDayOfWeek().getValue())) {
                        occurrences.add(new TaskOccurence(task.getTitle(), current.toString(), task.getStatus()));
                    }
                    current = current.plusDays(1);
                }
                case MONTHLY -> {
                    int day = recurrence.getDayOfMonth() == null ? current.getDayOfMonth() : recurrence.getDayOfMonth();
                    LocalDate monthlyDate = LocalDate.of(current.getYear(), current.getMonth(), Math.min(day, current.lengthOfMonth()));
                    if (!monthlyDate.isBefore(start) && !monthlyDate.isAfter(end)) {
                        occurrences.add(new TaskOccurence(task.getTitle(), monthlyDate.toString(), task.getStatus()));
                    }
                    current = current.plusMonths(recurrence.getInterval());
                }
                default -> current = end.plusDays(1);
            }
        }

        return occurrences.stream().distinct().collect(Collectors.toList());
    }

    private String now() {
        return LocalDateTime.now().withNano(0).toString();
    }

    private void exportEligibleTasksToICal(List<Task> tasks, String path) {
        List<Task> eligibleTasks = tasks.stream().filter(Task::hasDueDate).collect(Collectors.toList());
        if (eligibleTasks.isEmpty()) {
            System.out.println("No eligible tasks with due dates were found for iCal export.");
            return;
        }
        calendarGateway.exportTasks(eligibleTasks, path);
    }
}
