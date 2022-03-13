package tech.zeroreturn.microschema.configuration;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "service")
public class MicroSchema {
    
    private boolean cache = false;
    private List<Map<String, Object>> schemas = null;

    
    public boolean isCache() {
        return cache;
    }

    public void setCache(boolean cache) {
        this.cache = cache;
    }

   

    public List<Map<String, Object>> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<Map<String, Object>> schemas) {
        this.schemas = schemas;
    }



   
}
