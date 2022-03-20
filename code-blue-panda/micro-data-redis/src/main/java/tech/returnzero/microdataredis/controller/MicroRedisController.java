package tech.returnzero.microdataredis.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import tech.returnzero.microdatainterface.MicroDataControllerInterface;
import tech.returnzero.microdataredis.talker.RedisTalker;
import tech.returnzero.microexception.MicroException;

@RestController("/data/redis")
public class MicroRedisController implements MicroDataControllerInterface {

    @Autowired
    private RedisTalker talker;

    @Override
    @PostMapping
    public ResponseEntity<Object> operation(Map<String, Object> request) throws MicroException {   
        return this.databasesink(request, talker);
    }

}
