package ru.t1.taskmanager.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractEventProducer<T> {

    private final KafkaTemplate<String, Object> multiTypeKafkaTemplate;
    private final String kafkaTopic;

    protected AbstractEventProducer(KafkaTemplate<String, Object> multiTypeKafkaTemplate, String kafkaTopic) {
        this.multiTypeKafkaTemplate = multiTypeKafkaTemplate;
        this.kafkaTopic = kafkaTopic;
    }

    public CompletableFuture<SendResult<String, Object>> sendEvent(T kafkaEvent) {
        return multiTypeKafkaTemplate.send(kafkaTopic, kafkaEvent);
    }
}
