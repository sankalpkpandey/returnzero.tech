package tech.returnzero.greyhoundengine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tech.returnzero.greyhoundengine.database.DataBuilder;
import tech.returnzero.greyhoundengine.notification.EmailBuilder;
import tech.returnzero.greyhoundengine.request.RequestData;
import tech.returnzero.greyhoundengine.response.ResponseData;

@RestController
@RequestMapping("/api/operation")
public class OperationController {

    @Autowired
    private DataBuilder databuilder;

    @Autowired
    private EmailBuilder emailbuilder;

    @PostMapping("/work")
    public ResponseEntity<ResponseData> work(@RequestBody RequestData requestbody) {
        ResponseData response = new ResponseData();
        ResponseEntity<ResponseData> entity = null;
        try {
            response.setResponse(
                    databuilder.build(requestbody.getRequest(), requestbody.getOperation(), requestbody.getEntity()));
            entity = ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.setError(true);
            entity = new ResponseEntity<ResponseData>(response, HttpStatus.BAD_REQUEST);
        }
        return entity;
    }

    @PostMapping("/notify")
    public ResponseEntity<ResponseData> notify(@RequestBody RequestData requestbody) {
        ResponseData response = new ResponseData();
        ResponseEntity<ResponseData> entity = null;
        try {
            emailbuilder.build(requestbody.getRequest());
            entity = ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.setError(true);
            entity = new ResponseEntity<ResponseData>(response, HttpStatus.BAD_REQUEST);
        }
        return entity;
    }

}
