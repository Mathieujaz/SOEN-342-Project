package main;

import model.Collaborator;
import model.Task;
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
            System.out.println("5. Export all tasks to CSV");
            System.out.println("6. Export tasks to iCal");
            System.out.println("7. List overloaded collaborators");
            System.out.println("8. Update collaborator limits");
            System.out.println("9. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    service.reset();
                    System.out.println("Database cleared successfully.");
                    pause(scanner);
                    break;

                case "2":
                    System.out.print("Enter CSV file path: ");
                    String importPath = scanner.nextLine().trim();

                    if (importPath.isEmpty()) {
                        System.out.println("Import cancelled. Returning to menu.");
                    } else {
                        service.importCSV(importPath);
                    }

                    pause(scanner);
                    break;

                case "3":
                    System.out.println("\nSearch options:");
                    System.out.println("1. Keyword");
                    System.out.println("2. Status");
                    System.out.println("3. Priority");
                    System.out.println("4. Due date");
                    System.out.println("5. Date range");

                    String option = scanner.nextLine();
                    List<Task> results = null;

                    switch (option) {
                        case "1":
                            System.out.print("Keyword: ");
                            results = service.search(scanner.nextLine());
                            break;
                        case "2":
                            System.out.print("Status (OPEN/COMPLETED/CANCELLED): ");
                            results = service.searchByStatus(scanner.nextLine());
                            break;
                        case "3":
                            System.out.print("Priority (LOW/MEDIUM/HIGH): ");
                            results = service.searchByPriority(scanner.nextLine());
                            break;
                        case "4":
                            System.out.print("Date (YYYY-MM-DD): ");
                            results = service.searchByDate(scanner.nextLine());
                            break;
                        case "5":
                            System.out.print("Start date: ");
                            String start = scanner.nextLine();
                            System.out.print("End date: ");
                            String end = scanner.nextLine();
                            results = service.searchByDateRange(start, end);
                            break;
                    }

                    if (results != null) {
                        for (Task t : results) {
                            System.out.println(t);
                        }
                    }

                    break;

                case "4":
                    List<Task> tasks = service.getAll();

                    System.out.println("\nAll tasks:");
                    if (tasks.isEmpty()) {
                        System.out.println("No tasks in the database.");
                    } else {
                        for (Task task : tasks) {
                            System.out.println(task);
                        }
                    }

                    pause(scanner);
                    break;

                case "5":
                    System.out.print("Enter export file name or path: ");
                    String exportPath = scanner.nextLine().trim();

                    if (exportPath.isEmpty()) {
                        System.out.println("Export cancelled. Returning to menu.");
                    } else {
                        service.exportCSV(exportPath);
                    }

                    pause(scanner);
                    break;

                case "6":
                    handleIcalExport(scanner, service);
                    pause(scanner);
                    break;

                case "7":
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
                    break;

                case "8":
                    handleLimitUpdate(scanner, service);
                    pause(scanner);
                    break;

                case "9":
                    running = false;
                    System.out.println("Goodbye.");
                    break;

                default:
                    System.out.println("Invalid option. Please choose a number from 1 to 9.");
                    pause(scanner);
            }
        }

        scanner.close();
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
            case "1":
                System.out.print("Task ID: ");
                String taskIdInput = scanner.nextLine().trim();
                try {
                    service.exportSingleTaskToICal(Integer.parseInt(taskIdInput), exportPath);
                } catch (NumberFormatException e) {
                    System.out.println("Task ID must be a number.");
                }
                break;

            case "2":
                System.out.print("Project name: ");
                String projectName = scanner.nextLine().trim();
                if (projectName.isEmpty()) {
                    System.out.println("Project name is required.");
                    return;
                }
                service.exportProjectTasksToICal(projectName, exportPath);
                break;

            case "3":
                TaskFilter filter = buildFilterFromInput(scanner, service);
                if (filter != null) {
                    service.exportFilteredTasksToICal(filter, exportPath);
                }
                break;

            default:
                System.out.println("Invalid iCal export option.");
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
                case "1":
                    System.out.print("Category (SENIOR/INTERMEDIATE/JUNIOR): ");
                    String category = scanner.nextLine().trim();
                    System.out.print("New positive integer limit: ");
                    int limit = Integer.parseInt(scanner.nextLine().trim());
                    service.updateCollaboratorLimit(category, limit);
                    System.out.println("Updated limits: " + service.getCollaboratorLimitsSummary());
                    break;
                case "2":
                    service.resetCollaboratorLimits();
                    System.out.println("Limits reset. Current limits: " + service.getCollaboratorLimitsSummary());
                    break;
                default:
                    System.out.println("Invalid limit option.");
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
}
