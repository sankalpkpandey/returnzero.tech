package tech.returnzero.microdatainterface;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import tech.returnzero.microexception.MicroException;

public interface MicroDataControllerInterface {

    String PAYLOAD = "payload";
    String CRITERIA = "criteria";
    String OPERATION = "operation";
    String CREATE = "create";
    String GET = "get";
    String UPDATE = "update";
    String DELETE = "delete";
    String SINK = "sink";
    String SCHEMA = "schema";

    public ResponseEntity<Object> operation(Map<String, Object> payload) throws MicroException;

    @SuppressWarnings("unchecked")
    default ResponseEntity<Object> databasesink(Map<String, Object> request, MicroDataInterface talker)
            throws MicroException {
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
        } else if (SCHEMA.equals(operation)) {
            response = talker.schema(payload);
        }

        return new ResponseEntity<Object>(response, HttpStatus.OK);
    }
}
