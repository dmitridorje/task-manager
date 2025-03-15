package ru.t1.taskmanager.service.task;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.SendResult;
import ru.t1.taskmanager.kafka.producer.TaskUpdatedEventProducer;
import ru.t1.taskmanager.mapper.TaskMapper;
import ru.t1.taskmanager.model.dto.TaskDto;
import ru.t1.taskmanager.model.entity.Task;
import ru.t1.taskmanager.model.enums.TaskStatus;
import ru.t1.taskmanager.repository.TaskRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskUpdatedEventProducer taskUpdatedProducer;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task task;
    private TaskDto taskDto;

    @BeforeEach
    void setUp() {
        task = new Task(1L, "Test Task", "Description", 42L, TaskStatus.NEW);
        taskDto = new TaskDto(1L, "Test Task", "Description", 42L, TaskStatus.NEW);
    }

    @Test
    @DisplayName("Should return all tasks as DTOs")
    void testGetAllTasks_success() {
        when(taskRepository.findAll()).thenReturn(List.of(task));
        when(taskMapper.toDtoList(List.of(task))).thenReturn(List.of(taskDto));

        List<TaskDto> result = taskService.getAllTasks();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(taskDto, result.get(0));

        verify(taskRepository).findAll();
        verify(taskMapper).toDtoList(List.of(task));
    }

    @Test
    @DisplayName("Should return empty list when nothing is found")
    void testGetAllTasks_NoneFound() {
        when(taskRepository.findAll()).thenReturn(Collections.emptyList());
        when(taskMapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<TaskDto> result = taskService.getAllTasks();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository).findAll();
        verify(taskMapper).toDtoList(Collections.emptyList());
    }

    @Test
    @DisplayName("Should add a new task and return DTO")
    void testCreateTask_success() {
        when(taskMapper.toEntity(taskDto)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(taskDto);

        TaskDto result = taskService.addTask(taskDto);

        assertNotNull(result);
        assertEquals(taskDto.getId(), result.getId());
        assertEquals(taskDto.getTitle(), result.getTitle());
        assertEquals(taskDto.getDescription(), result.getDescription());
        assertEquals(taskDto.getStatus(), result.getStatus());
        assertEquals(taskDto.getUserId(), result.getUserId());

        verify(taskRepository).save(task);
        verify(taskMapper).toEntity(taskDto);
        verify(taskMapper).toDto(task);
    }

    @Test
    @DisplayName("Should return task DTO by ID")
    void testGetTaskById_success() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskMapper.toDto(task)).thenReturn(taskDto);

        TaskDto result = taskService.getTaskById(1L);

        assertNotNull(result);
        assertEquals(taskDto.getId(), result.getId());
        assertEquals(taskDto.getTitle(), result.getTitle());
        assertEquals(taskDto.getDescription(), result.getDescription());
        assertEquals(taskDto.getStatus(), result.getStatus());
        assertEquals(taskDto.getUserId(), result.getUserId());

        verify(taskRepository).findById(1L);
        verify(taskMapper).toDto(task);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when task is not found")
    void testGetTaskById_NotFound() {
        when(taskRepository.findById(108L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> taskService.getTaskById(108L));

        verify(taskRepository).findById(108L);
        verifyNoInteractions(taskMapper);
    }

    @Test
    @DisplayName("Should remove task by ID")
    void testRemoveTaskById_success() {
        when(taskRepository.deleteTaskByIdAndReturnStatus(1L)).thenReturn(true);

        assertDoesNotThrow(() -> taskService.removeTaskById(1L));

        verify(taskRepository).deleteTaskByIdAndReturnStatus(1L);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when removing a non-existing task")
    void testRemoveTaskById_nonExistingTask() {
        when(taskRepository.deleteTaskByIdAndReturnStatus(108L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> taskService.removeTaskById(108L));
    }


    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("Should update task and return updated DTO")
    void testUpdateTask_success() {
        TaskDto updatedTaskDto = new TaskDto(1L, "Updated Task", "Updated Description", 1L, TaskStatus.IN_PROGRESS);
        Task updatedTask = new Task(1L, "Updated Task", "Updated Description", 1L, TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(updatedTask);
        when(taskMapper.toDto(updatedTask)).thenReturn(updatedTaskDto);
        when(taskUpdatedProducer.sendEvent(argThat(event ->
                event.getId().equals(1L) && event.getStatus() == TaskStatus.IN_PROGRESS
        ))).thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        TaskDto result = taskService.updateTask(1L, updatedTaskDto);

        assertNotNull(result);
        assertEquals(updatedTaskDto.getTitle(), result.getTitle());
        assertEquals(updatedTaskDto.getDescription(), result.getDescription());
        assertEquals(updatedTaskDto.getStatus(), result.getStatus());
        assertEquals(updatedTaskDto.getUserId(), result.getUserId());

        verify(taskRepository).findById(1L);
        verify(taskRepository).save(task);
        verify(taskMapper).toDto(updatedTask);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when updating a non-existing task")
    void testUpdateTask_nonExistingTask() {
        when(taskRepository.findById(108L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> taskService.updateTask(108L, taskDto));

        verify(taskRepository).findById(108L);
        verifyNoInteractions(taskMapper);
    }
}
