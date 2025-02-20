package ru.t1.taskmanager.dao;

import ru.t1.taskmanager.model.dto.TaskDto;
import ru.t1.taskmanager.model.entity.Task;

import java.util.List;
import java.util.Optional;

public interface TaskDao {
    List<Task> getAllTasks();
    Task addTask(String title, String description, Long userId);
    Optional<Task> getTaskById(Long id);
    boolean removeTaskById(Long id);
    Task updateTask(Task taskToBeUpdated, TaskDto taskDto);
}
