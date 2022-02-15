package tech.returnzero.greyhoundengine.controller;

import java.util.Map;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.log4j.Log4j2;
import tech.returnzero.greyhoundengine.request.IntegrationRequest;
import tech.returnzero.greyhoundengine.response.ResponseData;
import tech.returnzero.greyhoundengine.restclient.RestClient;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/integration")
@Log4j2
public class IntegrationController {

    @Value("classpath:restclients.json")
    private Resource resourceFile;

    @Autowired
    private ObjectMapper jsonMapper;

    private Map<String, Object> restclientsconfig = null;

    @PostConstruct
    @SuppressWarnings("unchecked")
    public void build() {
        try {
            this.restclientsconfig = jsonMapper.readValue(resourceFile.getFile(), Map.class);
            log.debug("restclientsconfig {}", this.restclientsconfig);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @PostMapping("/call")
    @SuppressWarnings("unchecked")
    public ResponseEntity<ResponseData> call(@RequestBody IntegrationRequest integration) {
        ResponseData response = new ResponseData();

        if (this.restclientsconfig == null) {
            log.error("no client config found");
            response.setError(true);
            return ResponseEntity.badRequest().body(response);
        }

        Map<String, Object> clientconfig = (Map<String, Object>) this.restclientsconfig.get(integration.getName());

        RestClient client = RestClient.build().accept((String) clientconfig.get("accept"))
                .body(integration.getBody())
                .contenttype((String) clientconfig.get("contenttype"))
                .headers((Map<String, String>) clientconfig.get("headers")).url((String) clientconfig.get("url"));

        try {
            response.setResponse(client.work());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.setError(true);
            return ResponseEntity.badRequest().body(response);
        }

    }

}
