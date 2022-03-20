package tech.returnzero.microdataredis.talker;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import tech.returnzero.microdatainterface.MicroDataInterface;
import tech.returnzero.microexception.MicroException;

public class RedisTalker implements MicroDataInterface {

    // https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#appendix.application-properties.data

    @Autowired
    private RedisTemplate<String, Object> template;

    @Override
    public Object create(Map<String, Object> payload, String sinkname) throws MicroException {
        template.opsForValue().set(sinkname, payload);
        return 0;
    }

    @Override
    public Object get(Map<String, Object> criteria, String sinkname) throws MicroException {
        return template.opsForValue().get(sinkname);
    }

    @Override
    public Integer update(Map<String, Object> payload, Map<String, Object> criteria, String sinkname)
            throws MicroException {
        return (Integer) create(payload, sinkname);
    }

    @Override
    public Integer delete(Map<String, Object> criteria, String sinkname) throws MicroException {
        boolean deleted = template.delete(sinkname);
        return deleted ? 0 : 1;
    }

    @Override
    public Object custom(String type, Map<String, Object> payload) throws MicroException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean schema(Map<String, Object> sinkmap) {
        // TODO Auto-generated method stub
        return false;
    }

}
