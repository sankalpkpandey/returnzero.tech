package tech.returnzero.microdatamongo.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import tech.returnzero.microdatainterface.MicroDataControllerInterface;
import tech.returnzero.microdatamongo.talker.MongoTalker;
import tech.returnzero.microexception.MicroException;

@RestController("/data/mongo")
public class MicroMongoController implements MicroDataControllerInterface {

    @Autowired
    private MongoTalker talker = null;

    @Override
    @PostMapping
    public ResponseEntity<Object> operation(Map<String, Object> request) throws MicroException {
        return this.databasesink(request, talker);
    }

}
