package ru.t1.taskmanager.exception;

public class DaoException extends RuntimeException {
    public DaoException(String s, Exception e) {
        super(s, e);
    }
}
