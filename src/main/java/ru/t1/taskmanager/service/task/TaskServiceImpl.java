package ru.t1.taskmanager.service.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.taskmanager.aspect.annotation.CacheTask;
import ru.t1.taskmanager.aspect.annotation.LogAfterReturning;
import ru.t1.taskmanager.aspect.annotation.LogNotFoundException;
import ru.t1.taskmanager.aspect.annotation.MeasureExecutionTime;
import ru.t1.taskmanager.exception.EventPublishingException;
import ru.t1.taskmanager.kafka.producer.TaskUpdatedEventProducer;
import ru.t1.taskmanager.mapper.TaskMapper;
import ru.t1.taskmanager.model.dto.TaskDto;
import ru.t1.taskmanager.model.entity.Task;
import ru.t1.taskmanager.model.enums.TaskStatus;
import ru.t1.taskmanager.model.dto.TaskUpdatedEventDto;
import ru.t1.taskmanager.repository.TaskRepository;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class TaskServiceImpl implements TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskMapper taskMapper;
    private final TaskUpdatedEventProducer taskUpdatedProducer;
    private final TaskRepository taskRepository;

    @Autowired
    public TaskServiceImpl(TaskMapper taskMapper, TaskUpdatedEventProducer taskUpdatedProducer, TaskRepository taskRepository) {
        this.taskMapper = taskMapper;
        this.taskUpdatedProducer = taskUpdatedProducer;
        this.taskRepository = taskRepository;
    }

    @Override
    @LogAfterReturning
    public List<TaskDto> getAllTasks() {
        return taskMapper.toDtoList(taskRepository.findAll());
    }

    @Override
    @MeasureExecutionTime
    @Transactional
    public TaskDto addTask(TaskDto taskDto) {
        Task task = taskMapper.toEntity(taskDto);
        task.setStatus(TaskStatus.NEW);
        taskRepository.save(task);
        return taskMapper.toDto(task);
    }

    @Override
    @CacheTask(CacheTask.CacheOperation.READ)
    @LogNotFoundException
    public TaskDto getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id " + taskId));

        return taskMapper.toDto(task);
    }

    @Override
    @CacheTask(CacheTask.CacheOperation.EVICT)
    @LogNotFoundException
    @Transactional
    public void removeTaskById(Long taskId) {
        boolean isRemoved = taskRepository.deleteTaskByIdAndReturnStatus(taskId);

        if (!isRemoved) {
            throw new EntityNotFoundException("Task not found with id " + taskId);
        }
    }

    @Override
    @CacheTask(CacheTask.CacheOperation.UPDATE)
    @LogNotFoundException
    public TaskDto updateTask(Long taskId, TaskDto taskDto) {
        Task taskToBeUpdated = taskRepository.findById(taskId).orElseThrow(() ->
                new EntityNotFoundException("Task not found with id " + taskId));

        applyUpdates(taskToBeUpdated, taskDto);

        TaskDto updatedTask = taskMapper.toDto(taskRepository.save(taskToBeUpdated));

        TaskUpdatedEventDto event = new TaskUpdatedEventDto(taskId, updatedTask.getStatus());

        CompletableFuture<SendResult<String, Object>> future = taskUpdatedProducer.sendEvent(event);

        future.exceptionally(e -> {
            log.error("Error while sending updated task event: {}", e.getMessage());
            throw new EventPublishingException("Failed to send event to Kafka", e);
        }).thenAccept(result -> log.info("Successfully sent updated task event for task with ID {}",
                updatedTask.getId()));

        return updatedTask;
    }

    private void applyUpdates(Task task, TaskDto taskDto) {
        if (taskDto.getTitle() != null) {
            task.setTitle(taskDto.getTitle());
        }
        if (taskDto.getDescription() != null) {
            task.setDescription(taskDto.getDescription());
        }
        if (taskDto.getUserId() != null) {
            task.setUserId(taskDto.getUserId());
        }

        task.setStatus(taskDto.getStatus());
    }
}
