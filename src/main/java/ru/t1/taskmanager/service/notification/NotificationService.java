package ru.t1.taskmanager.service.notification;

import java.util.function.Function;

public interface NotificationService {
    <T> void send(T event, Function<T, String> messageProvider, Function<T, String> titleProvider);
}
