package ru.t1.taskmanager.kafka.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.t1.taskmanager.model.dto.TaskUpdatedEventDto;

@Component
public class TaskUpdatedEventProducer extends AbstractEventProducer<TaskUpdatedEventDto> {

    public TaskUpdatedEventProducer(KafkaTemplate<String, Object> multiTypeKafkaTemplate,
                                @Value("${spring.kafka.topics.update}") String kafkaTopic) {
        super(multiTypeKafkaTemplate, kafkaTopic);
    }
}
