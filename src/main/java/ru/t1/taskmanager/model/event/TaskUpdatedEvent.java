package ru.t1.taskmanager.model.event;

import ru.t1.taskmanager.model.enums.TaskStatus;

public class TaskUpdatedEvent {
    private Long id;
    private TaskStatus status;

    public TaskUpdatedEvent() {}

    public TaskUpdatedEvent(Long id, TaskStatus status) {
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
