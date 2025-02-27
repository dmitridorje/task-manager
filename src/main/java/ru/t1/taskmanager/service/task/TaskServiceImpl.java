package ru.t1.taskmanager.service.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import ru.t1.taskmanager.aspect.annotation.CacheTask;
import ru.t1.taskmanager.aspect.annotation.LogAfterReturning;
import ru.t1.taskmanager.aspect.annotation.LogNotFoundException;
import ru.t1.taskmanager.aspect.annotation.MeasureExecutionTime;
import ru.t1.taskmanager.dao.TaskDao;
import ru.t1.taskmanager.exception.EventPublishingException;
import ru.t1.taskmanager.kafka.producer.TaskUpdatedEventProducer;
import ru.t1.taskmanager.mapper.TaskMapper;
import ru.t1.taskmanager.model.dto.TaskDto;
import ru.t1.taskmanager.model.entity.Task;
import ru.t1.taskmanager.model.event.TaskUpdatedEvent;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class TaskServiceImpl implements TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskDao taskDao;
    private final TaskMapper taskMapper;
    private final TaskUpdatedEventProducer taskUpdatedProducer;

    @Autowired
    public TaskServiceImpl(TaskDao taskDao, TaskMapper taskMapper, TaskUpdatedEventProducer taskUpdatedProducer) {
        this.taskDao = taskDao;
        this.taskMapper = taskMapper;
        this.taskUpdatedProducer = taskUpdatedProducer;
    }

    @Override
    @LogAfterReturning
    public List<TaskDto> getAllTasks() {
        return taskMapper.toDtoList(taskDao.getAllTasks());
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

        TaskDto updatedTask = taskMapper.toDto(taskDao.updateTask(taskToBeUpdated, taskDto));

        TaskUpdatedEvent event = new TaskUpdatedEvent(taskId, updatedTask.getStatus());

        CompletableFuture<SendResult<String, Object>> future = taskUpdatedProducer.sendEvent(event);

        future.exceptionally(e -> {
            log.error("Error while sending updated task event: {}", e.getMessage());
            throw new EventPublishingException("Failed to send event to Kafka", e);
        }).thenAccept(result -> log.info("Successfully sent updated task event for task with ID {}",
                updatedTask.getId()));

        return updatedTask;
    }
}
