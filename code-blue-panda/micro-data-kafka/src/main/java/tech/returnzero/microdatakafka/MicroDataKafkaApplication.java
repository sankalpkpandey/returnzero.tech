package tech.returnzero.microdatakafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@SpringBootApplication
@EnableKafkaStreams
public class MicroDataKafkaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroDataKafkaApplication.class, args);
	}

}