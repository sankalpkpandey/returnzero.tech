package tech.returnzero.greyhoundengine.request;

import java.util.Map;

import lombok.Data;

@Data
public class RequestData {
    private Map<String, Object> request = null;
}
