package tech.returnzero.microdatainterface;

import java.util.Map;

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
}
