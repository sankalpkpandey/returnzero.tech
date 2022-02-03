package tech.returnzero.microrest.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfiguration {

    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
