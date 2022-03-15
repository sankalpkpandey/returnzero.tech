package tech.returnzero.microdatamongo.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import tech.returnzero.microdatainterface.MicroDataControllerInterface;
import tech.returnzero.microdatamongo.talker.MongoTalker;
import tech.returnzero.microexception.MicroException;

public class MicroMongoController implements MicroDataControllerInterface {

    @Autowired
    private MongoTalker talker = null;

    @Override
    public ResponseEntity<Object> operation(Map<String, Object> request) throws MicroException {
        return this.databasesink(request, talker);
    }

}
