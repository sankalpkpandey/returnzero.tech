package tech.returnzero.microdatakafka.consumer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import tech.returnzero.microdatainterface.MicroDataControllerInterface;
import tech.returnzero.microdatakafka.producer.MicroKafkaProducer;
import tech.returnzero.microexception.MicroException;

@Component
@Profile("kafkaconsumer")
// https://gist.github.com/geunho/77f3f9a112ea327457353aa407328771
public class MicroKafkaConsumer {

    // we need to define sink producers

    @Autowired(required = false)
    private MicroKafkaProducer sink;

    @Autowired
    private Environment env;

    private MicroDataControllerInterface datainterface = null;

    // consumer -> either produce to sink or directly consume.

    @PostConstruct
    public void init() throws Exception {
        String datainterfaceclass = env.getProperty("service.data.interface");
        if (datainterfaceclass != null) {
            this.datainterface = (MicroDataControllerInterface) Class.forName(datainterfaceclass)
                    .getDeclaredConstructors()[0].newInstance();
        }
    }

    @KafkaListener(topics = "#{'${service.kafka.consumers}'.split(',')}")
    @Profile("realtimeprocessing")
    public void consume(Map<String, Object> message) throws IOException {
        if (sink != null) {
            sink.sendMessage(message);
        } else {
            consumerToFactory(message);
        }
    }

    @KafkaListener(topics = "#{'${service.kafka.consumers}'.split(',')}", containerFactory = "batchFactory")
    @Profile("batchprocessing")
    public void consume(List<Map<String, Object>> messages) throws IOException {
        for (Map<String, Object> message : messages) {
            if (sink != null) {
                sink.sendMessage(message);
            } else {
                consumerToFactory(message);
            }
        }
    }

    private void consumerToFactory(Map<String, Object> message) {
        try {
            datainterface.operation(message);
        } catch (MicroException e) {
            e.printStackTrace();
        }
    }
}
