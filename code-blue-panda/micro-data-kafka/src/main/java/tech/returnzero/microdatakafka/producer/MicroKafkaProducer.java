package tech.returnzero.microdatakafka.producer;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("kafkaproducer")
public class MicroKafkaProducer {

    // need a generic producer .. this needs to know from where to pick data from
    // and push data to which topic

}
