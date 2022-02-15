package tech.returnzero.greyhoundengine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tech.returnzero.greyhoundengine.database.DataBuilder;
import tech.returnzero.greyhoundengine.notification.EmailBuilder;
import tech.returnzero.greyhoundengine.request.RequestData;
import tech.returnzero.greyhoundengine.response.ResponseData;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/operation")
public class OperationController {

    @Autowired
    private DataBuilder databuilder;

    @Autowired
    private EmailBuilder emailbuilder;

    @PostMapping("/work/{operation}/{entity}")
    public ResponseEntity<ResponseData> work(@RequestBody RequestData requestbody, @PathVariable String operation,
            @PathVariable String entity) {
        ResponseData response = new ResponseData();
        ResponseEntity<ResponseData> responseentity = null;
        try {
            databuilder.blocksensitives();
            response.setResponse(
                    databuilder.build(requestbody.getRequest(), operation, entity));
            responseentity = ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.setError(true);
            responseentity = new ResponseEntity<ResponseData>(response, HttpStatus.BAD_REQUEST);
        } finally {
            databuilder.unblocksesitives();
        }
        return responseentity;
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
