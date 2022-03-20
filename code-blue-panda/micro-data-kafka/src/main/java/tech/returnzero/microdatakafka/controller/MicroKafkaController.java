package tech.returnzero.microdatakafka.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import tech.returnzero.microdatainterface.MicroDataControllerInterface;
import tech.returnzero.microdatakafka.talker.KafkaTalker;
import tech.returnzero.microexception.MicroException;

@RestController("/data/kafka")
public class MicroKafkaController implements MicroDataControllerInterface{


    @Autowired
    private KafkaTalker talker;

    @Override
    @SuppressWarnings("unchecked")
    public ResponseEntity<Object> operation(Map<String, Object> request) throws MicroException {
        String sinkname = (String) request.get(SINK);
        Map<String, Object> payload = (Map<String, Object>) request.get(PAYLOAD);
        return   new ResponseEntity<Object>(talker.create(payload, sinkname), HttpStatus.OK);
    }
    
}
