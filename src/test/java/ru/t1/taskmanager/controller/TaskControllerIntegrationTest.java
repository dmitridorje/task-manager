package ru.t1.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.t1.taskmanager.kafka.producer.TaskUpdatedEventProducer;
import ru.t1.taskmanager.model.dto.TaskDto;
import ru.t1.taskmanager.model.dto.TaskUpdatedEventDto;
import ru.t1.taskmanager.model.entity.Task;
import ru.t1.taskmanager.model.enums.TaskStatus;
import ru.t1.taskmanager.repository.TaskRepository;
import ru.t1.taskmanager.util.ContainerCreator;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@Transactional
public class TaskControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    TaskRepository taskRepository;

    @MockitoBean
    private TaskUpdatedEventProducer taskUpdatedEventProducer;

    @Container
    private static final PostgreSQLContainer<?> postgresContainer = ContainerCreator.POSTGRES_CONTAINER;

    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void overrideSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgresContainer::getDriverClassName);
        registry.add("spring.liquibase.enabled", () -> false);
    }

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should add task and save entry in database")
    void testAddTask_Success() throws Exception {

        TaskDto newTask = new TaskDto("Sample Task", "Sample description", 42L, TaskStatus.NEW);

        String contentJson = objectMapper.writeValueAsString(newTask);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.title").value("Sample Task"))
                .andExpect(jsonPath("$.description").value("Sample description"))
                .andExpect(jsonPath("$.userId").value(42))
                .andExpect(jsonPath("$.status").value(TaskStatus.NEW.toString()))
                .andDo(MockMvcResultHandlers.print());

        Optional<Task> savedTaskOptional = taskRepository.findById(4L);

        Task savedTask = savedTaskOptional.orElseThrow(() ->
                new IllegalStateException("Expected a value, but Optional was empty"));

        assertNotNull(savedTask);
    }

    @Test
    @DisplayName("Should return all tasks")
    void testGetAllTasks_Success() throws Exception {
        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[1].description").value("Description for task 2"))
                .andExpect(jsonPath("$[2].userId").value(1003))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("Should return task by id")
    void testGetTaskById_Success() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Task 3"))
                .andExpect(jsonPath("$.description").value("Description for task 3"))
                .andExpect(jsonPath("$.userId").value(1003))
                .andExpect(jsonPath("$.status").value(TaskStatus.COMPLETED.toString()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("Should return 404 when task not found")
    void testGetTaskById_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/998"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Entity Not Found"))
                .andExpect(jsonPath("$.message").value("Task not found with id 998"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("Should delete task by id")
    void testDeleteTaskById_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/tasks/2"))
                .andExpect(status().isNoContent())
                .andDo(MockMvcResultHandlers.print());

        assertFalse(taskRepository.findById(2L).isPresent());
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existing task")
    void testDeleteTaskById_NotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Entity Not Found"))
                .andExpect(jsonPath("$.message").value("Task not found with id 999"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("Should update task by id")
    void testUpdateTaskById_Success() throws Exception {
        TaskDto updatedTask = new TaskDto("Updated Task", "Updated description", 1001L, TaskStatus.COMPLETED);
        String contentJson = objectMapper.writeValueAsString(updatedTask);
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(new SendResult<>(null, null));

        when(taskUpdatedEventProducer.sendEvent(any(TaskUpdatedEventDto.class)))
                .thenReturn(future);

        mockMvc.perform(put("/api/v1/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.status").value(TaskStatus.COMPLETED.toString()))
                .andDo(MockMvcResultHandlers.print());

        Optional<Task> optionalTaskAfterUpdate = taskRepository.findById(1L);
        Task savedTask = optionalTaskAfterUpdate.orElseThrow(() ->
                new IllegalStateException("Expected a value, but Optional was empty"));

        assertEquals("Updated Task", savedTask.getTitle());
        assertEquals("Updated description", savedTask.getDescription());
        assertEquals(TaskStatus.COMPLETED, savedTask.getStatus());
    }

    @Test
    @DisplayName("Should return 404 when updating non-existing task")
    void testUpdateTaskById_NotFound() throws Exception {
        TaskDto updatedTask = new TaskDto("Updated Task", "Updated description", 1001L, TaskStatus.COMPLETED);
        String contentJson = objectMapper.writeValueAsString(updatedTask);

        mockMvc.perform(put("/api/v1/tasks/997")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contentJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Entity Not Found"))
                .andExpect(jsonPath("$.message").value("Task not found with id 997"))
                .andDo(MockMvcResultHandlers.print());
    }
}
