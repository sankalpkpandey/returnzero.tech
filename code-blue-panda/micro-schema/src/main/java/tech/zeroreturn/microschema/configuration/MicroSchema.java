package tech.zeroreturn.microschema.configuration;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "service.data")
public class MicroSchema {
    
    private List<Map<String, Object>> schemas = null;

    public List<Map<String, Object>> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<Map<String, Object>> schemas) {
        this.schemas = schemas;
    }



   
}
