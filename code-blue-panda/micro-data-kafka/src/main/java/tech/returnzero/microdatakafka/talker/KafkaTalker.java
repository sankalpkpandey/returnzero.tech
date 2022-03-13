package tech.returnzero.microdatakafka.talker;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import tech.returnzero.microdatainterface.MicroDataInterface;
import tech.returnzero.microdatakafka.producer.MicroKafkaProducer;
import tech.returnzero.microexception.MicroException;

public class KafkaTalker implements MicroDataInterface {

    @Autowired(required = false)
    private MicroKafkaProducer producer;

    @Override
    public Object create(Map<String, Object> payload, String sinkname) throws MicroException {
        producer.sendMessage(sinkname, payload);
        return null;
    }

    @Override
    public Object get(Map<String, Object> criteria, String sinkname) throws MicroException {
        throw new MicroException("operation.not.supported");
    }

    @Override
    public Integer update(Map<String, Object> payload, Map<String, Object> criteria, String sinkname)
            throws MicroException {
        throw new MicroException("operation.not.supported");
    }

    @Override
    public Integer delete(Map<String, Object> criteria, String sinkname) throws MicroException {
        throw new MicroException("operation.not.supported");
    }

    @Override
    public Object custom(String type, Map<String, Object> payload) throws MicroException {
        throw new MicroException("operation.not.supported");
    }

    @Override
    public boolean schema(Map<String, Object> sinkmap) {
        return true;
    }

}
