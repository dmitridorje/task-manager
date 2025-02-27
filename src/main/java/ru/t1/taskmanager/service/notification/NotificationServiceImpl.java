package ru.t1.taskmanager.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class NotificationServiceImpl implements NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    String username;

    @Autowired
    public NotificationServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public <T> void send(T event, Function<T, String> messageProvider, Function<T, String> titleProvider) {
        String bodyText = messageProvider.apply(event);
        String subject = titleProvider.apply(event);
        log.info("Sending notification with the following subject: '{}'", subject);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("example@yandex.ru");
        message.setSubject(subject);
        message.setText(bodyText);
        message.setFrom(username);
        mailSender.send(message);

        log.info("Successfully sent notification with the following subject: '{}'", subject);
    }
}
