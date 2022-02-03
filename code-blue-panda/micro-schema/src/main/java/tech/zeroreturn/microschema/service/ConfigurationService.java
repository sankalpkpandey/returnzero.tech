package tech.zeroreturn.microschema.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tech.returnzero.microexception.MicroException;
import tech.zeroreturn.microschema.converter.QueryToPayload;
import tech.zeroreturn.microschema.talker.MicroTalker;

/**
 * @author phoenix
 * 
 *         Service to create , update , delete and read schema entities
 */

@Service
public class ConfigurationService {

    @Autowired
    private QueryToPayload queryToPayload = null;

    @Autowired
    private MicroTalker talker = null;

    public Object create(Map<String, String> payload, String sink) throws MicroException {
        return talker.create(payload, sink);
    }

    public Integer update(Map<String, String> payload, Map<String, String> query, String sink) throws MicroException {
        return talker.update(payload, queryToPayload.convert(query, sink), sink);
    }

    public Object get(Map<String, String> query, String sink) throws MicroException {
        return talker.get(queryToPayload.convert(query, sink), sink);
    }

    public Integer delete(Map<String, String> query, String sink) throws MicroException {
        return talker.delete(queryToPayload.convert(query, sink), sink);
    }
}