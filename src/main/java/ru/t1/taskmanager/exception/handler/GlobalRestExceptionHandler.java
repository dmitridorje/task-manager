package ru.t1.taskmanager.exception.handler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.t1.taskmanager.exception.TaskDtoValidationException;

import jakarta.persistence.EntityNotFoundException;
import java.util.Arrays;

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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        Throwable rootCause = ex.getRootCause();
        if (rootCause instanceof InvalidFormatException invalidFormatException) {
            if (invalidFormatException.getTargetType() != null && invalidFormatException.getTargetType().isEnum()) {
                String[] enumValues = Arrays.stream(invalidFormatException.getTargetType().getEnumConstants())
                        .map(Object::toString)
                        .toArray(String[]::new);
                String errorMessage = "Status must be one of the following: [" + String.join(", ", enumValues) + "]";
                log.error("Invalid enum value: {}", errorMessage, ex);
                return new ErrorResponse("Invalid Enum Value", errorMessage);
            }
        }
        log.error("Invalid request body", ex);
        return new ErrorResponse("Invalid Request Body", "The request body is invalid or malformed");
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleOtherRuntimeExceptions(RuntimeException ex) {
        log.error("Runtime Exception", ex);
        return new ErrorResponse("Runtime Exception", ex.getMessage());
    }
}
