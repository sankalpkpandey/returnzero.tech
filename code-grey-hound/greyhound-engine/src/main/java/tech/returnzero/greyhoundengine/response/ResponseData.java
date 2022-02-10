package tech.returnzero.greyhoundengine.response;

import lombok.Data;

@Data
public class ResponseData {
    private Object response = null;
    private boolean error = false;
}
