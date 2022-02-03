package tech.returnzero.microdatainterface;

import java.util.Map;

import tech.returnzero.microexception.MicroException;

public interface MicroDataInterface {

    public Object create(Map<String, Object> payload, String sinkname) throws MicroException;

    public Object get(Map<String, Object> criteria, String sinkname) throws MicroException;

    public Integer update(Map<String, Object> payload, Map<String, Object> criteria, String sinkname)
            throws MicroException;

    public Integer delete(Map<String, Object> criteria, String sinkname) throws MicroException;

    public Object custom(String type, Map<String, Object> payload) throws MicroException;

    public boolean schema(Map<String, Object> sinkmap);

}
