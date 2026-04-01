package service;

import model.Task;

import java.util.List;

public interface ICalendarGateway {
    void exportTasks(List<Task> tasks, String path);
}
