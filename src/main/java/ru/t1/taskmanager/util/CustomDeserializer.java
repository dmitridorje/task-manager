package ru.t1.taskmanager.util;

import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class CustomDeserializer<T> extends JsonDeserializer<T> {
    private static final Logger log = LoggerFactory.getLogger(CustomDeserializer.class);

    @Override
    public T deserialize(String topic, Headers headers, byte[] data) {
        try {
            return super.deserialize(topic, headers, data);
        } catch (Exception e) {
            log.warn("Error while deserializing message {}", new String(data, StandardCharsets.UTF_8));
            return null;
        }
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            return super.deserialize(topic, data);
        } catch (Exception e) {
            log.warn("Error while deserializing message {}", new String(data, StandardCharsets.UTF_8));
            return null;
        }
    }
}
