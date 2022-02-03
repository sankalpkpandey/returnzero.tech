package tech.returnzero.microdatamongo.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import tech.returnzero.microdatainterface.MicroDataControllerInterface;
import tech.returnzero.microdatamongo.talker.MongoTalker;
import tech.returnzero.microexception.MicroException;

public class MicroMongoController implements MicroDataControllerInterface {

    @Autowired
    private MongoTalker talker = null;

    @Override
    @SuppressWarnings("unchecked")
    public ResponseEntity<Object> operation(Map<String, Object> request) throws MicroException {

        String operation = (String) request.get(OPERATION);

        String sinkname = (String) request.get(SINK);
        Map<String, Object> payload = (Map<String, Object>) request.get(PAYLOAD);
        Map<String, Object> criteria = (Map<String, Object>) request.get(CRITERIA);

        Object response = null;

        if (GET.equals(operation)) {
            response = talker.get(criteria, sinkname);
        } else if (UPDATE.equals(operation)) {
            response = talker.update(payload, criteria, sinkname);
        } else if (CREATE.equals(operation)) {
            response = talker.create(payload, sinkname);
        } else if (DELETE.equals(operation)) {
            response = talker.delete(criteria, sinkname);
        }else if (SCHEMA.equals(operation)) {
            response = talker.schema(payload);
        }

        return new ResponseEntity<Object>(response, HttpStatus.OK);
    }

}
