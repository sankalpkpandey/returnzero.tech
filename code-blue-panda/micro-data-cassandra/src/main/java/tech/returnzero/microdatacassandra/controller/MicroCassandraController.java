package tech.returnzero.microdatacassandra.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import tech.returnzero.microdatacassandra.talker.CassandraTalker;
import tech.returnzero.microdatainterface.MicroDataControllerInterface;
import tech.returnzero.microexception.MicroException;

public class MicroCassandraController implements MicroDataControllerInterface {

    @Autowired
    private CassandraTalker talker;

    @Override
    public ResponseEntity<Object> operation(Map<String, Object> request) throws MicroException {
        return this.databasesink(request, talker);
    }

}
