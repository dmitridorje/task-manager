package ru.t1.taskmanager.service;

import ru.t1.taskmanager.model.dto.TaskDto;
import ru.t1.taskmanager.model.entity.Task;

import java.util.List;

public interface TaskService {
    List<Task> getAllTasks();
    TaskDto addTask(TaskDto taskDto);
    TaskDto getTaskById(Long id);
    void removeTaskById(Long id);
    TaskDto updateTask(Long taskId, TaskDto taskDto);
}
