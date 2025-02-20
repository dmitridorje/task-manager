package ru.t1.taskmanager.exception;

public class TaskDtoValidationException extends RuntimeException {
    public TaskDtoValidationException(String message) {
        super(message);
    }
}
