package tech.zeroreturn.microschema.talker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import tech.returnzero.microexception.MicroException;
import tech.returnzero.microrest.RestClient;
import tech.zeroreturn.microschema.configuration.SchemaService;

/**
 * @author sankalp
 * 
 *         Class to have interaction with datbase service as defined in config
 */
@Component
public class MicroTalker {

    @Value("service.data.url")
    private String dataserviceurl = null;

    private static final String PAYLOAD = "payload";
    private static final String CRITERIA = "criteria";

    private static final String OPERATION = "operation";
    private static final String CREATE = "create";
    private static final String GET = "get";
    private static final String UPDATE = "update";
    private static final String DELETE = "delete";
    private static final String SCHEMA = "schema";
    private static final String SINK = "sink";

    @Autowired
    private SchemaService schemaservice = null;

    public Object create(Map<String, String> payload, String sink) throws MicroException {
        return getData(Object.class, toRequest(schemaservice.getObject(sink, payload, false), null, CREATE, sink));
    }

    public List<?> get(Map<String, Object> search, String sink) throws MicroException {
        return getData(List.class, toRequest(null, search, GET, sink));
    }

    public Integer update(Map<String, String> payload, Map<String, Object> search, String sink) throws MicroException {
        return getData(Integer.class, toRequest(schemaservice.getObject(sink, payload, true), search, UPDATE, sink));
    }

    public Integer delete(Map<String, Object> search, String sink) throws MicroException {
        return getData(Integer.class, toRequest(null, search, DELETE, sink));
    }

    public Boolean schema(Map<String, Object> payload) throws MicroException {
        return getData(Boolean.class, toRequest(null, payload, SCHEMA, null));
    }

    private Map<String, Object> toRequest(Map<String, Object> payload, Map<String, Object> search, String operation,
            String sink) {
        Map<String, Object> request = new HashMap<>();
        request.put(PAYLOAD, payload);
        request.put(CRITERIA, search);
        request.put(OPERATION, operation);
        request.put(SINK, sink);
        return request;
    }

    private <T> T getData(Class<T> responseType, Map<String, Object> request) throws MicroException {
        try {
            return new RestClient<T>().url(this.dataserviceurl + OPERATION).request(request)
                    .responseType(responseType).call();
        } catch (RestClientException rs) {
            throw new MicroException("generic.database.failed");
        }
    }
}