package persistence;

import model.Priority;
import model.Task;
import model.TaskStatus;

import java.io.BufferedReader;
import java.io.FileReader;

public class CSVImporter {

    private TaskRepository repo;

    public CSVImporter(TaskRepository repo) {
        this.repo = repo;
    }

    public void importCSV(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {
                if (first) {
                    first = false;
                    continue;
                }

                String[] v = line.split(",");

                if (v.length < 5) {
                    System.out.println("A row in the CSV file is invalid and was skipped.");
                    continue;
                }

                Task t = new Task(
                        v[0].trim(),
                        v[1].trim(),
                        TaskStatus.valueOf(v[2].trim().toUpperCase()),
                        Priority.valueOf(v[3].trim().toUpperCase()),
                        v[4].trim()
                );

                repo.addTask(t);
            }

            System.out.println("Import completed successfully.");

        } catch (java.io.FileNotFoundException e) {
            System.out.println("File not found. Please enter a valid CSV file path.");
        } catch (IllegalArgumentException e) {
            System.out.println("The CSV file contains an invalid status or priority value.");
        } catch (Exception e) {
            System.out.println("Could not import the CSV file.");
        }
    }
}