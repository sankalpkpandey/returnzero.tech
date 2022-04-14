package tech.returnzero.greyhoundengine.request;

import java.util.Map;

import lombok.Data;

@Data
public class ScheduleRequest {

    private String integrationname = null;
    private Map<String, Object> requestbody = null;
    private Integer interval;
    private String unit;
    private String callbackemailtemplate = null;
    private Map<String, Object> emaildata = null;

}
