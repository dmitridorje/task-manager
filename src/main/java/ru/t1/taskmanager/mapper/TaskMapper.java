package ru.t1.taskmanager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.t1.taskmanager.model.dto.TaskDto;
import ru.t1.taskmanager.model.entity.Task;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskMapper {
    TaskDto toDto(Task task);
    @Mapping(target = "id", ignore = true)
    Task toEntity(TaskDto taskDto);
    List<TaskDto> toDtoList(List<Task> tasks);
}
