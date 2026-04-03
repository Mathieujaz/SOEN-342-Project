# SOEN-342-Project

Mathieu Jazrawi  - 40284648

Wendy Mbog       - 40259555

Jennifer Avakian - 40263197


# Task Management System (Proof of Concept)

## Overview

This project is a Java proof of concept for a personal task management system developed for SOEN 342. It supports task organization, project grouping, tags, subtasks, collaborator assignment, recurrence, CSV import/export, iCal export, and persistent storage through SQLite.


The system integrates:

* **Java** for implementation
* **SQLite** for persistent storage
* **CSV files** for data import and export
*  **iCalendar (`.ics`) ** export for external calendar applications

---

## Features

### Task Management
The system allows users to:

- Create tasks manually
- Update task title, description, status, priority, and due date
- View all tasks
- Move tasks between projects or remove them from a project
- Divide tasks into subtasks
- Add tags to tasks
- View activity history for a task

### Project Management
The system supports:

- Creating projects
- Viewing all projects
- Grouping tasks into projects
- Exporting all tasks in a given project to iCal

### Search and View
The system allows users to search tasks using multiple criteria:

- Keyword
- Status (`OPEN`, `COMPLETED`, `CANCELLED`)
- Priority (`LOW`, `MEDIUM`, `HIGH`)
- Specific due date
- Date range
- Project
- Tag

### Collaborators
The system supports collaborator assignment on tasks:

- Assign a collaborator to a task
- Collaborator categories:
  - `SENIOR`
  - `INTERMEDIATE`
  - `JUNIOR`
- Category-based open-task limits:
  - `SENIOR = 2`
  - `INTERMEDIATE = 5`
  - `JUNIOR = 10`
- Listing overloaded collaborators
- Updating collaborator limits at runtime for overload scenarios

When a collaborator is assigned to a task, a collaborator-linked subtask is also created.

### Recurrence and Occurrences
The system supports recurring tasks:

- Daily recurrence
- Weekly recurrence on selected weekdays
- Monthly recurrence on a selected day
- Viewing generated task occurrences

Occurrences use the same task name with different due dates.

### Import from CSV
The user can specify a CSV file path and import tasks into the system.

The CSV import supports richer task data including:

- task title
- description
- status
- priority
- due date
- project name
- project description
- collaborator name
- collaborator category
- tags
- subtasks
- recurrence pattern

If needed, related records such as projects are created automatically during import.

### Export to CSV
The system can export all tasks stored in the database into a CSV file.

### iCal Export
The system supports exporting task data to iCalendar (`.ics`) format for use in common calendar applications such as Google Calendar, Apple Calendar, and Outlook.

Supported iCal export modes:

- Export a single task
- Export all tasks in a project
- Export a filtered list of tasks

Rules:

- Only tasks with a due date are exported
- Each eligible task becomes one calendar entry
- The calendar entry includes:
  - title
  - description
  - due date
  - status
  - priority
  - project name
- If a task has subtasks, a summary of those subtasks is included in the event description
- Subtasks are not exported as separate calendar entries

### Persistency
The system uses an SQLite database (`tasks.db`).

- Data remains stored between executions unless the database is reset
- Tasks and projects are persisted

## Project Structure

```
src/
 ├─ model/         # Task, Project, Tag, Subtask, Collaborator, Recurrence, History
 ├─ persistence/   # SQLite setup, repository, CSV and iCal gateways
 ├─ service/       # Business logic (TaskService, gateways)
 └─ main/          # Console menu and user interaction
```

---

## How to Run

### 1. Compile the project

```bash
javac -cp ".;sqlite-jdbc-3.51.3.0.jar" src\model\*.java src\persistence\*.java src\service\*.java src\main\Main.java
```

### 2. Run the application

```bash
java -cp ".;sqlite-jdbc-3.51.3.0.jar;src" main.Main
```

---

## CSV Format

Example of a valid `tasks.csv` file:

```
title,description,status,priority,dueDate,projectName,projectDescription,collaboratorName,collaboratorCategory,tags,subtasks,recurrencePattern
Finish report,Write the final report,OPEN,HIGH,2026-04-10,School,Course assignments,Bob,INTERMEDIATE,school;report,[ ] Write intro; [ ] Write conclusion,WEEKLY|1|2026-04-10|2026-04-24|1,3,5|
Study quiz,Review chapters,OPEN,MEDIUM,2026-04-12,School,Course assignments,,,study,[ ] Review notes,DAILY|2|2026-04-12|2026-04-20||
Submit work,Upload files,COMPLETED,HIGH,2026-04-01,,,,,submission,,

```

---

## Usage

When the program starts, a menu is displayed:

```
1. reset Database
2. Import tasks from CSV
3. Search tasks
4. View all tasks
5. Create project
6. View all projects
7. Create task
8. Update task
9. Move task to project
10. Add tag to task
11. Add subtask to task
12. Assign collaborator to task
13. View task activity history
14. Configure task recurrence
15. View task occurrences
16. Export all tasks to CSV
17. Export tasks to iCal
18. List overloaded collaborators
19. Update collaborator limits
20. Exit

```

---

## Design Overview

The system follows a layered architecture:

* **Model layer**: represents the core entities such as tasks, projects, subtasks, tags, collaborators, activity history, and recurrence patterns
* **Persistence layer**: handles SQLite storage and file-based CSV / iCal import-export
* **Service layer**: coordinates business logic, filtering, recurrence handling, collaborator rules, and export logic
* **Main layer**: handles user interaction through a console menu

---

## Notes

* This project is a proof of concept and not a full production system
* Error handling is simplified for clarity
* CSV import assumes a reasonably well-formed file
* The SQLite database file (tasks.db) is generated locally during execution
* Generated files such as .class, .ics, and tasks.db should generally not be committed to Git
