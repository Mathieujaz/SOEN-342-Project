package service;

import model.Collaborator;
import model.CollaboratorCategory;
import model.Priority;
import model.Task;
import model.TaskStatus;
import persistence.CSVExporter;
import persistence.CSVImporter;
import persistence.ICalendarFileGateway;
import persistence.TaskRepository;

import java.time.LocalDate;
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

    public List<Task> getAll() {
        return repo.getAllTasks();
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
        if (category == null) {
            throw new IllegalArgumentException("Invalid collaborator category.");
        }
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

        if (keyword != null && !keyword.isBlank()) {
            filter.withKeyword(keyword.trim());
        }
        if (status != null && !status.isBlank()) {
            filter.withStatus(TaskStatus.valueOf(status.trim().toUpperCase()));
        }
        if (priority != null && !priority.isBlank()) {
            filter.withPriority(Priority.valueOf(priority.trim().toUpperCase()));
        }
        if (startDate != null && !startDate.isBlank()) {
            filter.withStartDate(LocalDate.parse(startDate.trim()));
        }
        if (endDate != null && !endDate.isBlank()) {
            filter.withEndDate(LocalDate.parse(endDate.trim()));
        }

        return filter;
    }

    private void exportEligibleTasksToICal(List<Task> tasks, String path) {
        List<Task> eligibleTasks = tasks.stream()
                .filter(Task::hasDueDate)
                .collect(Collectors.toList());

        if (eligibleTasks.isEmpty()) {
            System.out.println("No eligible tasks with due dates were found for iCal export.");
            return;
        }

        calendarGateway.exportTasks(eligibleTasks, path);
    }
}
