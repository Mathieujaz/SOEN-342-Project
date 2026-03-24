package service;

import model.Task;
import persistence.CSVExporter;
import persistence.CSVImporter;
import persistence.TaskRepository;

import java.util.List;

public class TaskService {

    private TaskRepository repo = new TaskRepository();
    private CSVImporter importer = new CSVImporter(repo);
    private CSVExporter exporter = new CSVExporter(repo);

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

    public void reset() {
        repo.deleteAll();
    }
}