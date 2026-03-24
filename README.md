# SOEN-342-Project

Mathieu Jazrawi  - 40284648

Wendy Mbog       - 40259555

Jennifer Avakian - 40263197


# Task Management System (Proof of Concept)

## Overview

This project is a **proof of concept (POC)** for a task management system developed in Java.
It demonstrates how tasks can be imported, stored, searched, viewed, and exported using a simple architecture.

The system integrates:

* **Java** for implementation
* **SQLite** for persistent storage
* **CSV files** for data import and export

---

## Features

### Task Search and View

The system allows users to view tasks using multiple criteria:

* Search by **keyword** (title or description)
* View tasks by **status** (OPEN, COMPLETED, CANCELLED)
* View tasks by **priority** (LOW, MEDIUM, HIGH)
* View tasks by a specific **due date**
* View tasks within a **date range**

---

### Import from CSV

* The user specifies a CSV file path
* Tasks are read and inserted into the system
* The system can be reset beforehand to start fresh

---

### Export to CSV

* All tasks stored in the database are exported
* Output is written into a CSV file

---

### Persistency

* The system uses an **SQLite database (`tasks.db`)**
* Data remains stored between executions unless reset

---

## Project Structure

```
src/
 ├─ model/         # Task, Priority, TaskStatus
 ├─ persistence/   # Database, CSV handling, Repository
 ├─ service/       # Business logic (TaskService)
 └─ main/          # Entry point (Main menu)
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
title,description,status,priority,dueDate
Do project,Finish report,OPEN,HIGH,2026-03-30
Meeting,Team sync,OPEN,MEDIUM,2026-03-25
Submit work,Upload files,COMPLETED,HIGH,2026-03-20
```

---

## Usage

When the program starts, a menu is displayed:

```
1. Start fresh
2. Import tasks from CSV
3. Search tasks
4. View all tasks
5. Export tasks to CSV
6. Exit
```

### Start fresh

Clears all existing tasks from the database.

### Import

Loads tasks from a CSV file into the database.

### Search

Allows users to filter tasks using different criteria.

### View

Displays all tasks currently stored.

### Export

Writes all tasks to a CSV file.

---

## Design Overview

The system follows a layered architecture:

* **Model layer** → Represents task data (`Task`, `Priority`, `TaskStatus`)
* **Persistence layer** → Handles database and CSV operations
* **Service layer** → Coordinates logic and operations
* **Main layer** → Handles user interaction via a console menu

---

## Notes

* This project is a **proof of concept**, not a full production system
* Error handling is simplified for clarity
* CSV parsing is basic and assumes a well-formatted file
