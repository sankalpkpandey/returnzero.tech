package tech.returnzero.microdatajdbc.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import tech.returnzero.microdatainterface.MicroDataControllerInterface;
import tech.returnzero.microdatajdbc.talker.JDBCTalker;
import tech.returnzero.microexception.MicroException;

public class MicroJDBController implements MicroDataControllerInterface {

    @Autowired
    private JDBCTalker talker;

    @Override
    public ResponseEntity<Object> operation(Map<String, Object> request) throws MicroException {
        return this.databasesink(request, talker);
    }

}
