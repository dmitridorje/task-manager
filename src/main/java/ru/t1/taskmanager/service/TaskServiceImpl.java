package ru.t1.taskmanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.t1.taskmanager.aspect.annotation.CacheTask;
import ru.t1.taskmanager.aspect.annotation.LogAfterReturning;
import ru.t1.taskmanager.aspect.annotation.LogNotFoundException;
import ru.t1.taskmanager.aspect.annotation.MeasureExecutionTime;
import ru.t1.taskmanager.dao.TaskDao;
import ru.t1.taskmanager.mapper.TaskMapper;
import ru.t1.taskmanager.model.dto.TaskDto;
import ru.t1.taskmanager.model.entity.Task;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
    private final TaskDao taskDao;
    private final TaskMapper taskMapper;

    @Autowired
    public TaskServiceImpl(TaskDao taskDao, TaskMapper taskMapper) {
        this.taskDao = taskDao;
        this.taskMapper = taskMapper;
    }

    @Override
    @LogAfterReturning
    public List<Task> getAllTasks() {
        return taskDao.getAllTasks();
    }

    @Override
    @MeasureExecutionTime
    public TaskDto addTask(TaskDto taskDto) {
        Task task = taskDao.addTask(taskDto.getTitle(), taskDto.getDescription(), taskDto.getUserId());
        return taskMapper.toDto(task);
    }

    @Override
    @CacheTask(CacheTask.CacheOperation.READ)
    @LogNotFoundException
    public TaskDto getTaskById(Long taskId) {
        Task task = taskDao.getTaskById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id " + taskId));

        return taskMapper.toDto(task);
    }

    @Override
    @CacheTask(CacheTask.CacheOperation.EVICT)
    @LogNotFoundException
    public void removeTaskById(Long taskId) {
        boolean isRemoved = taskDao.removeTaskById(taskId);

        if (!isRemoved) {
            throw new EntityNotFoundException("Task not found with id " + taskId);
        }
    }

    @Override
    @CacheTask(CacheTask.CacheOperation.UPDATE)
    @LogNotFoundException
    public TaskDto updateTask(Long taskId, TaskDto taskDto) {
        Task taskToBeUpdated = taskDao.getTaskById(taskId).orElseThrow(() ->
                new EntityNotFoundException("Task not found with id " + taskId));
        return taskMapper.toDto(taskDao.updateTask(taskToBeUpdated, taskDto));
    }
}
