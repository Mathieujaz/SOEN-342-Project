package main;

import model.Task;
import persistence.DatabaseInitializer;
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
            System.out.println("6. Exit");
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
                            System.out.print("Status (OPEN/COMPLETED): ");
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
                    running = false;
                    System.out.println("Goodbye.");
                    break;

                default:
                    System.out.println("Invalid option. Please choose a number from 1 to 6.");
                    pause(scanner);
            }
        }

        scanner.close();
    }
}