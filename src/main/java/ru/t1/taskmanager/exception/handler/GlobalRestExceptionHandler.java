package ru.t1.taskmanager.exception.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.t1.taskmanager.exception.TaskDtoValidationException;

import javax.persistence.EntityNotFoundException;

@RestControllerAdvice
public class GlobalRestExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalRestExceptionHandler.class);

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleEntityNotFound(EntityNotFoundException ex) {
        log.error("Entity not found", ex);
        return new ErrorResponse("Entity Not Found", ex.getMessage());
    }

    @ExceptionHandler(TaskDtoValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTaskDtoValidation(TaskDtoValidationException ex) {
        log.error("Task DTO validation failed", ex);
        return new ErrorResponse("Task DTO validation failed", ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleOtherRuntimeExceptions(RuntimeException ex) {
        log.error("Runtime Exception", ex);
        return new ErrorResponse("Runtime Exception", ex.getMessage());
    }
}
