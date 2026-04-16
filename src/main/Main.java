package main;

import model.ActivityHistory;
import model.Collaborator;
import model.Project;
import model.RecurrencePattern;
import model.Task;
import model.TaskOccurence;
import persistence.DatabaseInitializer;
import service.TaskFilter;
import service.TaskService;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void pause(Scanner scanner) {
        System.out.println("\nPress Enter to return to the menu...");
        scanner.nextLine();
    }

    public static void main(String[] args) {
        DatabaseInitializer.initialize();

        TaskService service = new TaskService();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n========== TASK MENU ==========");
            System.out.println("1. reset Database");
            System.out.println("2. Import tasks from CSV");
            System.out.println("3. Search tasks");
            System.out.println("4. View all tasks");
            System.out.println("5. Create project");
            System.out.println("6. View all projects");
            System.out.println("7. Create task");
            System.out.println("8. Update task");
            System.out.println("9. Move task to project");
            System.out.println("10. Add tag to task");
            System.out.println("11. Add subtask to task");
            System.out.println("12. Assign collaborator to task");
            System.out.println("13. View task activity history");
            System.out.println("14. Configure task recurrence");
            System.out.println("15. View task occurrences");
            System.out.println("16. Export all tasks to CSV");
            System.out.println("17. Export tasks to iCal");
            System.out.println("18. List overloaded collaborators");
            System.out.println("19. Update collaborator limits");
            System.out.println("20. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    service.reset();
                    System.out.println("Database cleared successfully.");
                    pause(scanner);
                }
                case "2" -> {
                    System.out.print("Enter CSV file path: ");
                    String importPath = scanner.nextLine().trim();
                    if (importPath.isEmpty()) {
                        System.out.println("Import cancelled. Returning to menu.");
                    } else {
                        service.importCSV(importPath);
                    }
                    pause(scanner);
                }
                case "3" -> handleSearch(scanner, service);
                case "4" -> {
                    printTasks(service.getAll());
                    pause(scanner);
                }
                case "5" -> {
                    System.out.print("Project name: ");
                    String name = scanner.nextLine().trim();
                    System.out.print("Project description (optional): ");
                    String description = scanner.nextLine().trim();
                    if (service.createProject(name, description)) {
                        System.out.println("Project created successfully.");
                    } else {
                        System.out.println("Could not create project.");
                    }
                    pause(scanner);
                }
                case "6" -> {
                    List<Project> projects = service.getAllProjects();
                    if (projects.isEmpty()) {
                        System.out.println("No projects found.");
                    } else {
                        projects.forEach(System.out::println);
                    }
                    pause(scanner);
                }
                case "7" -> handleCreateTask(scanner, service);
                case "8" -> handleUpdateTask(scanner, service);
                case "9" -> handleMoveTask(scanner, service);
                case "10" -> {
                    System.out.print("Task ID: ");
                    int taskId = Integer.parseInt(scanner.nextLine().trim());
                    System.out.print("Tag name: ");
                    String tagName = scanner.nextLine().trim();
                    System.out.println(service.addTagToTask(taskId, tagName) ? "Tag added." : "Could not add tag.");
                    pause(scanner);
                }
                case "11" -> {
                    System.out.print("Task ID: ");
                    int taskId = Integer.parseInt(scanner.nextLine().trim());
                    System.out.print("Subtask title: ");
                    String subtaskTitle = scanner.nextLine().trim();
                    System.out.println(service.addSubtaskToTask(taskId, subtaskTitle) ? "Subtask added." : "Could not add subtask.");
                    pause(scanner);
                }
                case "12" -> {
                    System.out.print("Task ID: ");
                    int taskId = Integer.parseInt(scanner.nextLine().trim());
                    System.out.print("Collaborator name: ");
                    String collaboratorName = scanner.nextLine().trim();
                    System.out.print("Category (SENIOR/INTERMEDIATE/JUNIOR): ");
                    String category = scanner.nextLine().trim();
                    System.out.println(service.assignCollaboratorToTask(taskId, collaboratorName, category) ? "Collaborator assigned." : "Could not assign collaborator.");
                    pause(scanner);
                }
                case "13" -> {
                    System.out.print("Task ID: ");
                    int taskId = Integer.parseInt(scanner.nextLine().trim());
                    List<ActivityHistory> history = service.getTaskActivityHistory(taskId);
                    if (history.isEmpty()) {
                        System.out.println("No activity history found.");
                    } else {
                        history.forEach(System.out::println);
                    }
                    pause(scanner);
                }
                case "14" -> handleRecurrence(scanner, service);
                case "15" -> {
                    System.out.print("Task ID: ");
                    int taskId = Integer.parseInt(scanner.nextLine().trim());
                    List<TaskOccurence> occurrences = service.getTaskOccurrences(taskId, 10);
                    if (occurrences.isEmpty()) {
                        System.out.println("No occurrences found.");
                    } else {
                        occurrences.forEach(System.out::println);
                    }
                    pause(scanner);
                }
                case "16" -> {
                    System.out.print("Enter export file name or path: ");
                    String exportPath = scanner.nextLine().trim();
                    if (exportPath.isEmpty()) {
                        System.out.println("Export cancelled. Returning to menu.");
                    } else {
                        service.exportCSV(exportPath);
                    }
                    pause(scanner);
                }
                case "17" -> {
                    handleIcalExport(scanner, service);
                    pause(scanner);
                }
                case "18" -> {
                    List<Collaborator> collaborators = service.getOverloadedCollaborators();
                    if (collaborators.isEmpty()) {
                        System.out.println("No overloaded collaborators were found.");
                    } else {
                        System.out.println("Overloaded collaborators:");
                        for (Collaborator collaborator : collaborators) {
                            System.out.println("- " + collaborator.getName() + " | Category: " + collaborator.getCategory()
                                    + " | Open tasks: " + collaborator.getOpenTaskCount()
                                    + " | Limit: " + collaborator.getOpenTaskLimit());
                        }
                    }
                    pause(scanner);
                }
                case "19" -> {
                    handleLimitUpdate(scanner, service);
                    pause(scanner);
                }
                case "20" -> {
                    running = false;
                    System.out.println("Goodbye.");
                }
                default -> {
                    System.out.println("Invalid option. Please choose a number from 1 to 20.");
                    pause(scanner);
                }
            }
        }

        scanner.close();
    }

    private static void handleSearch(Scanner scanner, TaskService service) {
        System.out.println("\nSearch options:");
        System.out.println("1. Keyword");
        System.out.println("2. Status");
        System.out.println("3. Priority");
        System.out.println("4. Due date");
        System.out.println("5. Date range");
        System.out.println("6. Project");
        System.out.println("7. Tag");
        String option = scanner.nextLine().trim();
        List<Task> results = null;

        switch (option) {
            case "1" -> {
                System.out.print("Keyword: ");
                results = service.search(scanner.nextLine());
            }
            case "2" -> {
                System.out.print("Status (OPEN/COMPLETED/CANCELLED): ");
                results = service.searchByStatus(scanner.nextLine());
            }
            case "3" -> {
                System.out.print("Priority (LOW/MEDIUM/HIGH): ");
                results = service.searchByPriority(scanner.nextLine());
            }
            case "4" -> {
                System.out.print("Date (YYYY-MM-DD): ");
                results = service.searchByDate(scanner.nextLine());
            }
            case "5" -> {
                System.out.print("Start date: ");
                String start = scanner.nextLine();
                System.out.print("End date: ");
                String end = scanner.nextLine();
                results = service.searchByDateRange(start, end);
            }
            case "6" -> {
                System.out.print("Project name: ");
                results = service.searchByProject(scanner.nextLine());
            }
            case "7" -> {
                System.out.print("Tag: ");
                results = service.searchByTag(scanner.nextLine());
            }
        }

        printTasks(results);
        pause(scanner);
    }

    private static void handleCreateTask(Scanner scanner, TaskService service) {
        try {
            System.out.print("Title: ");
            String title = scanner.nextLine().trim();
            System.out.print("Description: ");
            String description = scanner.nextLine().trim();
            System.out.print("Status (OPEN/COMPLETED/CANCELLED): ");
            String status = scanner.nextLine().trim();
            System.out.print("Priority (LOW/MEDIUM/HIGH): ");
            String priority = scanner.nextLine().trim();
            System.out.print("Due date (YYYY-MM-DD, optional): ");
            String dueDate = scanner.nextLine().trim();
            System.out.print("Project name (optional): ");
            String projectName = scanner.nextLine().trim();
            String projectDescription = "";
            if (!projectName.isBlank()) {
                System.out.print("Project description (optional): ");
                projectDescription = scanner.nextLine().trim();
            }
            System.out.println(service.createTask(title, description, status, priority, dueDate, projectName, projectDescription)
                    ? "Task created successfully."
                    : "Could not create task.");
        } catch (Exception e) {
            System.out.println("Could not create task.");
        }
        pause(scanner);
    }

    private static void handleUpdateTask(Scanner scanner, TaskService service) {
        try {
            System.out.print("Task ID: ");
            int taskId = Integer.parseInt(scanner.nextLine().trim());
            Task task = service.getTaskById(taskId);
            if (task == null) {
                System.out.println("Task not found.");
            } else {
                System.out.print("Title [" + task.getTitle() + "]: ");
                String title = readOrDefault(scanner, task.getTitle());
                System.out.print("Description [" + task.getDescription() + "]: ");
                String description = readOrDefault(scanner, task.getDescription());
                System.out.print("Status [" + task.getStatus() + "]: ");
                String status = readOrDefault(scanner, task.getStatus().name());
                System.out.print("Priority [" + task.getPriority() + "]: ");
                String priority = readOrDefault(scanner, task.getPriority().name());
                System.out.print("Due date [" + task.getDueDate() + "]: ");
                String dueDate = readOrDefault(scanner, task.getDueDate());
                System.out.println(service.updateTaskBasics(taskId, title, description, status, priority, dueDate)
                        ? "Task updated successfully."
                        : "Could not update task.");
            }
        } catch (Exception e) {
            System.out.println("Could not update task.");
        }
        pause(scanner);
    }

    private static void handleMoveTask(Scanner scanner, TaskService service) {
        try {
            System.out.print("Task ID: ");
            int taskId = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("New project name (blank to remove from project): ");
            String projectName = scanner.nextLine().trim();
            String projectDescription = "";
            if (!projectName.isBlank()) {
                System.out.print("Project description (optional): ");
                projectDescription = scanner.nextLine().trim();
            }
            System.out.println(service.moveTaskToProject(taskId, projectName, projectDescription)
                    ? "Task moved successfully."
                    : "Could not move task.");
        } catch (Exception e) {
            System.out.println("Could not move task.");
        }
        pause(scanner);
    }

    private static void handleRecurrence(Scanner scanner, TaskService service) {
        try {
            System.out.print("Task ID: ");
            int taskId = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Type (NONE/DAILY/WEEKLY/MONTHLY): ");
            String type = scanner.nextLine().trim().toUpperCase();
            if (type.equals("NONE")) {
                System.out.println(service.setTaskRecurrence(taskId, RecurrencePattern.none()) ? "Recurrence removed." : "Could not update recurrence.");
            } else {
                System.out.print("Interval: ");
                int interval = Integer.parseInt(scanner.nextLine().trim());
                System.out.print("Start date (YYYY-MM-DD): ");
                String startDate = scanner.nextLine().trim();
                System.out.print("End date (YYYY-MM-DD, optional): ");
                String endDate = scanner.nextLine().trim();
                List<Integer> weekdays = new java.util.ArrayList<>();
                Integer dayOfMonth = null;
                if (type.equals("WEEKLY")) {
                    System.out.print("Weekdays as numbers 1-7 separated by commas (optional): ");
                    String weekdayInput = scanner.nextLine().trim();
                    if (!weekdayInput.isBlank()) {
                        for (String value : weekdayInput.split(",")) {
                            weekdays.add(Integer.parseInt(value.trim()));
                        }
                    }
                }
                if (type.equals("MONTHLY")) {
                    System.out.print("Day of month: ");
                    dayOfMonth = Integer.parseInt(scanner.nextLine().trim());
                }
                RecurrencePattern pattern = new RecurrencePattern(RecurrencePattern.Type.valueOf(type), interval, startDate, endDate, weekdays, dayOfMonth);
                System.out.println(service.setTaskRecurrence(taskId, pattern) ? "Recurrence updated." : "Could not update recurrence.");
            }
        } catch (Exception e) {
            System.out.println("Could not update recurrence.");
        }
        pause(scanner);
    }

    private static void handleIcalExport(Scanner scanner, TaskService service) {
        System.out.println("\niCal export options:");
        System.out.println("1. Export a single task");
        System.out.println("2. Export all tasks in a project");
        System.out.println("3. Export a filtered list of tasks");
        System.out.print("Choose an option: ");

        String option = scanner.nextLine().trim();

        System.out.print("Enter export file name or path (.ics): ");
        String exportPath = scanner.nextLine().trim();
        if (exportPath.isEmpty()) {
            System.out.println("Export cancelled. Returning to menu.");
            return;
        }

        switch (option) {
            case "1" -> {
                System.out.print("Task ID: ");
                String taskIdInput = scanner.nextLine().trim();
                try {
                    service.exportSingleTaskToICal(Integer.parseInt(taskIdInput), exportPath);
                } catch (NumberFormatException e) {
                    System.out.println("Task ID must be a number.");
                }
            }
            case "2" -> {
                System.out.print("Project name: ");
                String projectName = scanner.nextLine().trim();
                if (projectName.isEmpty()) {
                    System.out.println("Project name is required.");
                    return;
                }
                service.exportProjectTasksToICal(projectName, exportPath);
            }
            case "3" -> {
                TaskFilter filter = buildFilterFromInput(scanner, service);
                if (filter != null) {
                    service.exportFilteredTasksToICal(filter, exportPath);
                }
            }
            default -> System.out.println("Invalid iCal export option.");
        }
    }

    private static void handleLimitUpdate(Scanner scanner, TaskService service) {
        System.out.println("Current limits: " + service.getCollaboratorLimitsSummary());
        System.out.println("1. Update one category limit");
        System.out.println("2. Reset limits to defaults");
        System.out.print("Choose an option: ");

        String option = scanner.nextLine().trim();

        try {
            switch (option) {
                case "1" -> {
                    System.out.print("Category (SENIOR/INTERMEDIATE/JUNIOR): ");
                    String category = scanner.nextLine().trim();
                    System.out.print("New positive integer limit: ");
                    int limit = Integer.parseInt(scanner.nextLine().trim());
                    service.updateCollaboratorLimit(category, limit);
                    System.out.println("Updated limits: " + service.getCollaboratorLimitsSummary());
                }
                case "2" -> {
                    service.resetCollaboratorLimits();
                    System.out.println("Limits reset. Current limits: " + service.getCollaboratorLimitsSummary());
                }
                default -> System.out.println("Invalid limit option.");
            }
        } catch (Exception e) {
            System.out.println("Could not update collaborator limits.");
        }
    }

    private static TaskFilter buildFilterFromInput(Scanner scanner, TaskService service) {
        try {
            System.out.print("Keyword (optional): ");
            String keyword = scanner.nextLine();
            System.out.print("Status (OPEN/COMPLETED/CANCELLED, optional): ");
            String status = scanner.nextLine();
            System.out.print("Priority (LOW/MEDIUM/HIGH, optional): ");
            String priority = scanner.nextLine();
            System.out.print("Start date (YYYY-MM-DD, optional): ");
            String startDate = scanner.nextLine();
            System.out.print("End date (YYYY-MM-DD, optional): ");
            String endDate = scanner.nextLine();
            return service.buildFilter(keyword, status, priority, startDate, endDate);
        } catch (Exception e) {
            System.out.println("The filter values are invalid.");
            return null;
        }
    }

    private static void printTasks(List<Task> tasks) {
        System.out.println("\nTasks:");
        if (tasks == null || tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }

        for (Task task : tasks) {
            System.out.println(task);
            if (task.getDescription() != null && !task.getDescription().isBlank()) {
                System.out.println("   Description: " + task.getDescription());
            }

            if (!task.getSubtasks().isEmpty()) {
                System.out.println("   Subtasks:");
                task.getSubtasks().forEach(subtask -> System.out.println("   - " + subtask));
            }
        }
    }

    private static String readOrDefault(Scanner scanner, String currentValue) {
        String value = scanner.nextLine();
        return value.isBlank() ? currentValue : value.trim();
    }
}

