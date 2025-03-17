package ru.t1.taskmanager.model.dto;

import ru.t1.taskmanager.model.enums.TaskStatus;

public class TaskDto {
    private Long id;
    private String title;
    private String description;
    private Long userId;
    private TaskStatus status;

    public TaskDto() {
    }

    public TaskDto(Long id, String title, String description, Long userId, TaskStatus taskStatus) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.userId = userId;
        this.status = taskStatus;
    }

    public TaskDto(String title, String description, Long userId, TaskStatus taskStatus) {
        this.title = title;
        this.description = description;
        this.userId = userId;
        this.status = taskStatus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "TaskDto{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", userId=" + userId +
                ", status=" + status +
                '}';
    }
}
