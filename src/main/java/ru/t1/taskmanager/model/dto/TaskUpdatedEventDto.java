package ru.t1.taskmanager.model.dto;

import ru.t1.taskmanager.model.enums.TaskStatus;

public class TaskUpdatedEventDto {
    private Long id;
    private TaskStatus status;

    public TaskUpdatedEventDto() {}

    public TaskUpdatedEventDto(Long id, TaskStatus status) {
        this.id = id;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "TaskUpdatedEvent{" +
                "id=" + id +
                ", status=" + status +
                '}';
    }
}
