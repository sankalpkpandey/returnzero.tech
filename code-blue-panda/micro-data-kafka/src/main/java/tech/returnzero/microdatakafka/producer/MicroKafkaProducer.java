package tech.returnzero.microdatakafka.producer;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("kafkaproducer")
public class MicroKafkaProducer {

    @Autowired
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    @Value("service.kafka.producer")
    private String producertopic = null;

    public void sendMessage(Map<String, Object> message) {
        this.kafkaTemplate.send(producertopic, message);
    }

    public void sendMessage(String producertopic, Map<String, Object> message) {
        this.kafkaTemplate.send(producertopic, message);
    }

}
