package ru.t1.taskmanager.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.t1.taskmanager.aspect.annotation.CacheTask;
import ru.t1.taskmanager.exception.TaskDtoValidationException;
import ru.t1.taskmanager.model.dto.TaskDto;

import jakarta.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Aspect
@Component
public class TaskAspects {
    private static final Logger log = LoggerFactory.getLogger(TaskAspects.class);
    private final Map<Long, TaskDto> cachedTasks = new HashMap<>();

    @AfterThrowing(
            pointcut = "@annotation(ru.t1.taskmanager.aspect.annotation.LogNotFoundException)",
            throwing = "exception"
    )
    public void logNotFoundExceptions(JoinPoint joinPoint, EntityNotFoundException exception) {
        log.error("EntityNotFoundException in {}: {}", joinPoint.getSignature().toShortString(),
                exception.getMessage(), exception);
    }

    @AfterReturning(
            pointcut = "@annotation(ru.t1.taskmanager.aspect.annotation.LogAfterReturning)",
            returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        if (result instanceof List<?> tasks) {
            log.info("Method '{}' returned {} tasks", joinPoint.getSignature().toShortString(), tasks.size());
        }
    }

    @Before("execution(* ru.t1.taskmanager.controller.TaskController.createTask(..)) && args(taskDto)")
    public void validateDtoBeforeCreation(TaskDto taskDto) {
        String title = taskDto.getTitle();
        String description = taskDto.getDescription();
        Long userId = taskDto.getUserId();

        if (title == null || title.isEmpty()) {
            throw new TaskDtoValidationException("Task title cannot be empty");
        }
        if (description == null || description.isEmpty()) {
            throw new TaskDtoValidationException("Task description cannot be empty");
        }
        if (userId == null) {
            throw new TaskDtoValidationException("User ID cannot be null");
        }

        log.info("TaskDto validated successfully before creating task, taskDto: title={}, description={}, userId={}",
                taskDto.getTitle(), taskDto.getDescription(), taskDto.getUserId());
    }

    @Before(value = "execution(* ru.t1.taskmanager.controller.TaskController.updateTaskById(..)) && args(id, taskDto)",
            argNames = "id,taskDto")
    public void validateDtoBeforeUpdate(Long id, TaskDto taskDto) {
        String title = taskDto.getTitle();
        String description = taskDto.getDescription();

        if (title != null && title.trim().isEmpty()) {
            throw new TaskDtoValidationException("Title cannot be blank");
        }
        if (description != null && description.trim().isEmpty()) {
            throw new TaskDtoValidationException("Description cannot be blank");
        }

        log.info("TaskDto validated successfully before updating task with ID {}", id);
    }

    @Around("@annotation(ru.t1.taskmanager.aspect.annotation.MeasureExecutionTime)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        log.info("Execution time of {}: {} ms", joinPoint.getSignature().toShortString(), executionTime);

        return result;
    }

    @Around("@annotation(cacheTask)")
    public Object handleCache(ProceedingJoinPoint joinPoint, CacheTask cacheTask) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Long taskId = (Long) args[0];
        Object result;

        switch (cacheTask.value()) {
            case READ:
                if (cachedTasks.containsKey(taskId)) {
                    result = cachedTasks.get(taskId);
                } else {
                    TaskDto taskDto = (TaskDto) joinPoint.proceed();
                    cachedTasks.put(taskId, taskDto);
                    log.info("Cache added for task with ID {} during execution of {}",
                            taskId, joinPoint.getSignature().toShortString());
                    result = taskDto;
                }
                return result;

            case UPDATE:
                boolean existsBefore = cachedTasks.containsKey(taskId);
                result = joinPoint.proceed();
                cachedTasks.put(taskId, (TaskDto) result);

                log.info("Cache {} for task with ID {} during execution of {}",
                        existsBefore ? "updated" : "added",
                        taskId, joinPoint.getSignature().toShortString());

                return result;

            case EVICT:
                if (cachedTasks.containsKey(taskId)) {
                    cachedTasks.remove(taskId);
                    log.info("Cache evicted for task with ID {} during execution of {}",
                            taskId, joinPoint.getSignature().toShortString());
                }

                return joinPoint.proceed();

            default:
                throw new IllegalArgumentException("Unknown cache operation: " + cacheTask.value());
        }
    }
}
