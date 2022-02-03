package tech.zeroreturn.microschema.converter;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tech.returnzero.microexception.MicroException;
import tech.zeroreturn.microschema.configuration.SchemaService;

@Component
public class QueryToPayload {

  @Autowired
  private SchemaService schemaservice = null;

  @SuppressWarnings("unchecked")
  public Map<String, Object> convert(Map<String, String> query, String sink) throws MicroException {

    Map<String, Object> convertedMap = new HashMap<>();

    Map<String, Object> schema = schemaservice.schema(sink);
    Map<String, Map<String, Object>> properties = (Map<String, Map<String, Object>>) schema.get("properties");

    for (Entry<String, String> queryentry : query.entrySet()) {

      if (queryentry.getKey().equals("sink")) {
        continue;
      }

      String propertyname = queryentry.getKey();
      Object[] propertyvalue = queryentry.getValue().split(":");
      Map<String, Object> propertymap = properties.get(propertyname);

      if (propertymap == null) {
        throw new MicroException("validation.schema.property.notfound");
      }

      String classtype = (String) propertymap.get("type");
      String subtype = (String) propertymap.get("subtype");
      propertyvalue[1] = schemaservice.valueOf(classtype, subtype, (String) propertyvalue[1]);
      convertedMap.put(queryentry.getKey(), propertyvalue);
    }

    return convertedMap;
  }

}
