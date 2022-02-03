package tech.zeroreturn.microschema.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tech.returnzero.microexception.MicroException;
import tech.zeroreturn.microschema.service.ConfigurationService;

/**
 * @author phoenix
 * 
 *         Controller to create , update , delete and read schema entities
 */
@RestController("/config")
public class ConfigurationController {

    @Autowired
    private ConfigurationService configService = null;

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody Map<String, String> payload,
            @RequestParam Map<String, String> query)
            throws MicroException {
        return toEntity(configService.create(payload, query.get("sink")));
    }

    @PostMapping
    public ResponseEntity<Integer> update(@RequestBody Map<String, String> payload,
            @RequestParam Map<String, String> query)
            throws MicroException {
        return toEntity(configService.update(payload, query, query.get("sink")));
    }

    @DeleteMapping
    public ResponseEntity<Integer> delete(@RequestParam Map<String, String> query)
            throws MicroException {
        return toEntity(configService.delete(query, query.get("sink")));
    }

    @GetMapping
    public ResponseEntity<Object> get(@RequestParam Map<String, String> query)
            throws MicroException {
        return toEntity(configService.get(query, query.get("sink")));
    }

    private <T> ResponseEntity<T> toEntity(T response) {
        return new ResponseEntity<T>(response, HttpStatus.OK);
    }

}
