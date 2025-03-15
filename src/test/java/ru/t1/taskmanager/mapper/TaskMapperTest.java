package ru.t1.taskmanager.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.t1.taskmanager.model.dto.TaskDto;
import ru.t1.taskmanager.model.entity.Task;
import ru.t1.taskmanager.model.enums.TaskStatus;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaskMapperTest {
    private final TaskMapper mapper = new TaskMapperImpl();

    @Test
    @DisplayName("mapping to dto")
    public void toTaskDtoTest() {
        Task entity = getEntity();
        TaskDto dto = mapper.toDto(entity);

        assertAll(
                () -> assertNotNull(dto),
                () -> assertEquals(entity.getTitle(), dto.getTitle()),
                () -> assertEquals(entity.getDescription(), dto.getDescription()),
                () -> assertEquals(entity.getUserId(), dto.getUserId()),
                () -> assertEquals(entity.getStatus(), dto.getStatus())
        );
    }

    @Test
    @DisplayName("mapping to dto, null input")
    public void toTaskDtoNullTest() {
        TaskDto dto = mapper.toDto(null);

        assertNull(dto);
    }

    @Test
    @DisplayName("mapping to entity")
    public void toTaskEntityTest() {
        TaskDto dto = getDto();
        Task entity = mapper.toEntity(dto);

        assertAll(
                () -> assertNotNull(entity),
                () -> assertEquals(dto.getTitle(), entity.getTitle()),
                () -> assertEquals(dto.getDescription(), entity.getDescription()),
                () -> assertEquals(dto.getUserId(), entity.getUserId()),
                () -> assertEquals(dto.getStatus(), entity.getStatus())
        );
    }

    @Test
    @DisplayName("mapping to entity, null input")
    public void toTaskEntityNullTest() {
        Task entity = mapper.toEntity(null);

        assertNull(entity);
    }

    @Test
    @DisplayName("mapping to entity list")
    public void toDtoListTest() {
        List<Task> taskList = List.of(getEntity(), getEntity());

        List<TaskDto> dtoList = mapper.toDtoList(taskList);

        assertAll(
                () -> assertNotNull(dtoList),
                () -> assertEquals(taskList.size(), dtoList.size()),
                () -> assertEquals(taskList.get(0).getTitle(), dtoList.get(0).getTitle()),
                () -> assertEquals(taskList.get(0).getDescription(), dtoList.get(0).getDescription()),
                () -> assertEquals(taskList.get(0).getUserId(), dtoList.get(0).getUserId()),
                () -> assertEquals(taskList.get(0).getStatus(), dtoList.get(0).getStatus()),
                () -> assertEquals(taskList.get(1).getTitle(), dtoList.get(1).getTitle()),
                () -> assertEquals(taskList.get(1).getDescription(), dtoList.get(1).getDescription()),
                () -> assertEquals(taskList.get(1).getUserId(), dtoList.get(1).getUserId()),
                () -> assertEquals(taskList.get(1).getStatus(), dtoList.get(1).getStatus())
        );
    }

    @Test
    @DisplayName("mapping to entity list, empty input")
    public void toDtoListEmptyTest() {

        List<TaskDto> result = mapper.toDtoList(Collections.emptyList());

        assertAll(
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );
    }

    private Task getEntity() {
        Task entity = new Task();
        entity.setTitle("Task 1");
        entity.setDescription("Description for task 1");
        entity.setUserId(42L);
        entity.setStatus(TaskStatus.NEW);
        return entity;
    }

    private TaskDto getDto() {
        TaskDto dto = new TaskDto();
        dto.setTitle("Task 1");
        dto.setDescription("Description for task 1");
        dto.setUserId(42L);
        dto.setStatus(TaskStatus.NEW);
        return dto;
    }
}
