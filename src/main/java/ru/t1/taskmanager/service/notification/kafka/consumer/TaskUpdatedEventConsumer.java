package ru.t1.taskmanager.service.notification.kafka.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.t1.taskmanager.model.dto.TaskUpdatedEventDto;
import ru.t1.taskmanager.service.notification.NotificationService;

import java.util.List;

@Component
@KafkaListener(
        topics = "${spring.kafka.topics.update}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "batchKafkaListenerContainerFactory"
)
public class TaskUpdatedEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(TaskUpdatedEventConsumer.class);

    private final NotificationService notificationService;

    @Autowired
    public TaskUpdatedEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaHandler
    public void listen(List<TaskUpdatedEventDto> events, Acknowledgment ack) {
        log.info("Received {} task updated event(s)", events.size());
        boolean allProcessedSuccessfully = true;

        for (TaskUpdatedEventDto event : events) {
            try {
                notificationService.send(event,
                        e -> "This is to notify on status change for task with ID " + e.getId() +
                                ", new status: " + e.getStatus(),
                        e -> "Update for task with ID " + e.getId());
            } catch (Exception e) {
                allProcessedSuccessfully = false;
                log.error("Error processing event: {}", event, e);
            }
        }
        if (allProcessedSuccessfully) {
            ack.acknowledge();
        } else {
            log.warn("Not all events were processed successfully, acknowledgment is not sent");
        }
    }
}
