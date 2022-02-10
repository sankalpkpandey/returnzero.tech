package tech.returnzero.greyhoundengine.request;

import java.util.Map;

import lombok.Data;

@Data
public class RequestData {
    private Map<String, Object> request = null;
    private String entity = null;
    private String operation = null;
}
