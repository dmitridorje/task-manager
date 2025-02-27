package ru.t1.taskmanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.taskmanager.model.dto.TaskDto;
import ru.t1.taskmanager.service.task.TaskService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskDto> getAllTasks() {
        return taskService.getAllTasks();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDto createTask(@RequestBody TaskDto taskDto) {
        return taskService.addTask(taskDto);
    }

    @GetMapping("/{taskId}")
    public TaskDto getTaskById(@PathVariable Long taskId) {
        return taskService.getTaskById(taskId);
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTaskById(@PathVariable Long taskId) {
        taskService.removeTaskById(taskId);
    }

    @PutMapping("/{taskId}")
    public TaskDto updateTaskById(@PathVariable Long taskId, @RequestBody TaskDto taskDto) {
        return taskService.updateTask(taskId, taskDto);
    }
}
