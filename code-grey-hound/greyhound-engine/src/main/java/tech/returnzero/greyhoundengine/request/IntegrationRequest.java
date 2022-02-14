package tech.returnzero.greyhoundengine.request;

import java.util.Map;

import lombok.Data;

@Data
public class IntegrationRequest {

    private String name = null;
    private Map<String, Object> body = null;

}
