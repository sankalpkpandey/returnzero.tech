package tech.zeroreturn.microschema.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import tech.returnzero.microcontext.UserContext;
import tech.returnzero.microexception.MicroException;
import tech.zeroreturn.microschema.talker.MicroTalker;

@Component
public class SchemaService {

    @Autowired
    private MicroSchema schemconfig;

    @Autowired
    private MicroTalker talker = null;

    @PostConstruct
    public void build() throws MicroException {

        Map<String, Object> sinkmap = new HashMap<>();
        List<Map<String, Object>> schemas = schemconfig.getSchemas();
        for (Map<String, Object> schema : schemas) {
            String schematype = (String) schema.get("type");
            sinkmap.put(schematype, schema);
        }

        talker.schema(sinkmap);

    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getObject(String type, Map<String, String> payload, boolean update)
            throws MicroException {

        Map<String, Object> foundschema = schema(type);

        Map<String, Map<String, Object>> properties = (Map<String, Map<String, Object>>) foundschema.get("properties");
        Map<String, Map<String, Object>> defaults = (Map<String, Map<String, Object>>) foundschema.get("defaults");
        Map<String, Map<String, Object>> rules = (Map<String, Map<String, Object>>) foundschema.get("rules");

        Map<String, Object> schemaObject = toObject(properties, defaults, payload, update);

        if (!update) {
            // if create only then call for constraints check
            Map<String, Object> constraints = (Map<String, Object>) foundschema
                    .get("constraints");
            if (constraints != null) {
                checkconstraints(constraints, properties, schemaObject, type);
            }
        }

        if (rules != null) {
            // if rules are defined for schema
            runrules(properties, rules, schemaObject);
        }

        return schemaObject;
    }

    private Map<String, Object> toObject(Map<String, Map<String, Object>> properties,
            Map<String, Map<String, Object>> defaults, Map<String, String> payload, boolean update)
            throws MicroException {

        Map<String, Object> emptyobject = create(properties, defaults, update);
        objectFromPayload(emptyobject, payload, properties);

        return emptyobject;

    }

    @SuppressWarnings("unchecked")
    private void runrules(Map<String, Map<String, Object>> properties, Map<String, Map<String, Object>> rules,
            Map<String, Object> schemaObject) throws MicroException {

        for (Map.Entry<String, Map<String, Object>> rulesentryset : rules.entrySet()) {
            String propertyname = rulesentryset.getKey();

            if (properties.get(propertyname) == null) {
                throw new MicroException("validation.schema.property.notfound");
            }

            Map<String, Object> checks = rulesentryset.getValue();
            for (Map.Entry<String, Object> checkentryset : checks.entrySet()) {
                String checkname = checkentryset.getKey();

                Object value = schemaObject.get(propertyname);

                if ("length".equals(checkname)) {
                    if (String.class.isAssignableFrom(value.getClass())) {
                        Integer length = ((String) value).length();
                        Map<String, Integer> minmax = (Map<String, Integer>) checkentryset.getValue();
                        Integer min = minmax.get("min");
                        Integer max = minmax.get("max");

                        if (min != null && length < min.intValue()) {
                            throw new MicroException("validation.contract.minlength.failed",
                                    new String[] { propertyname, min.toString(), length.toString(),
                                            (String) value });
                        }

                        if (max != null && length > max.intValue()) {
                            throw new MicroException("validation.contract.maxlength.failed",
                                    new String[] { propertyname, max.toString(), length.toString(),
                                            (String) value });
                        }

                    } else {
                        throw new MicroException("validation.rule.notsupported",
                                new String[] { propertyname, checkname });
                    }

                } else if ("regex".equals(checkname)) {

                    if (String.class.isAssignableFrom(value.getClass())) {
                        Map<String, String> regexmap = (Map<String, String>) checkentryset.getValue();
                        String regex = regexmap.get("regex");
                        if (!((String) value).matches(regex)) {
                            throw new MicroException("validation.contract.regex.failed",
                                    new String[] { propertyname, regex, (String) value });
                        }

                    } else {
                        throw new MicroException("validation.rule.notsupported",
                                new String[] { propertyname, checkname });
                    }

                } else if ("bound".equals(checkname)) {
                    Double comparablevalue = null;
                    try {
                        comparablevalue = Double.valueOf(value.toString());
                    } catch (NumberFormatException n) {
                        throw new MicroException("validation.rule.notsupported",
                                new String[] { propertyname, checkname });
                    }

                    Map<String, Double> minmax = (Map<String, Double>) checkentryset.getValue();
                    Double min = minmax.get("min");
                    Double max = minmax.get("max");

                    if (min != null && comparablevalue.doubleValue() < min.doubleValue()) {
                        throw new MicroException("validation.contract.minbound.failed",
                                new String[] { propertyname, min.toString(), comparablevalue.toString() });
                    }

                    if (max != null && comparablevalue.doubleValue() > max.doubleValue()) {
                        throw new MicroException("validation.contract.maxlength.failed",
                                new String[] { propertyname, max.toString(), comparablevalue.toString() });
                    }

                }
            }

        }
    }

    @SuppressWarnings("unchecked")
    private void checkconstraints(Map<String, Object> constraints, Map<String, Map<String, Object>> properties,
            Map<String, Object> emptyobject, String type) throws MicroException {
        for (Map.Entry<String, Object> constraintsentryset : constraints.entrySet()) {

            String constraintame = constraintsentryset.getKey();

            if ("notnull".equals(constraintame)) {

                List<String> attributes = (List<String>) constraintsentryset.getValue();

                for (String attribute : attributes) {
                    Map<String, Object> propertymap = properties.get(attribute);
                    if (propertymap == null) {
                        throw new MicroException("validation.schema.property.notfound");
                    }

                    String classtype = (String) propertymap.get("type");

                    if (emptyobject.get(attribute) == null) {
                        throw new MicroException("validation.contract.notnull.failed",
                                new String[] { attribute });
                    }
                    if ("array".equals(classtype)) {
                        if (((List<?>) emptyobject.get(attribute)).isEmpty()) {
                            throw new MicroException("validation.contract.notnull.failed",
                                    new String[] { attribute });
                        }
                    }
                }

            } else if ("unique".equals(constraintame)) {

                Map<String, Object> search = new HashMap<>();
                List<String> attributes = (List<String>) constraintsentryset.getValue();
                for (String attribute : attributes) {

                    Map<String, Object> propertymap = properties.get(attribute);
                    if (propertymap == null) {
                        throw new MicroException("validation.schema.property.notfound");
                    }
                    search.put(attribute, new Object[] { "is", emptyobject.get(attribute) });
                }

                List<?> data = talker.get(search, type);
                if (data != null && !data.isEmpty()) {
                    throw new MicroException("validation.contract.unique.failed",
                            new String[] { StringUtils.collectionToDelimitedString(attributes, ",") });
                }

            } else {
                throw new MicroException("validation.constraint.notsupported");
            }

        }
    }

    private void objectFromPayload(Map<String, Object> emptyobject, Map<String, String> payload,
            Map<String, Map<String, Object>> properties) throws MicroException {

        for (Map.Entry<String, String> payloadentry : payload.entrySet()) {
            String fieldname = payloadentry.getKey();
            String fieldvalue = payloadentry.getValue();
            Map<String, Object> propertymap = properties.get(fieldname);

            if (propertymap == null) {
                throw new MicroException("validation.schema.property.notfound");
            }

            String classtype = (String) propertymap.get("type");
            String subtype = (String) propertymap.get("subtype");
            emptyobject.put(fieldname, valueOf(classtype, subtype, fieldvalue));
        }

    }

    public Object valueOf(String classtype, String subtype, String value) throws MicroException {
        try {

            if (classtype.equals("array")) {
                String[] values = value.split(",");
                List<Object> valueList = new ArrayList<>(values.length);
                for (String arrval : values) {
                    Class<?> classinput = Class.forName("java.lang." + StringUtils.capitalize(subtype));
                    Object arrobj = classinput.getMethod("valueOf", String.class).invoke(null, arrval);
                    valueList.add(arrobj);
                }
                return valueList;
            } else if (classtype.equals("map")) {
                return null;
            } else {
                Class<?> classinput = Class.forName("java.lang." + StringUtils.capitalize(classtype));
                return classinput.getMethod("valueOf", String.class).invoke(null, value);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new MicroException("validation.schema.type.notfound", new String[] { classtype });
        }

    }

    public Map<String, Object> schema(String type) throws MicroException {

        List<Map<String, Object>> schemas = schemconfig.getSchemas();
        Map<String, Object> foundschema = null;
        for (Map<String, Object> schema : schemas) {
            String schematype = (String) schema.get("type");
            if (type.equals(schematype)) {
                foundschema = schema;
                break;
            }
        }

        if (foundschema == null) {
            throw new MicroException("validation.schema.notfound");
        }

        return foundschema;
    }

    private Map<String, Object> create(Map<String, Map<String, Object>> properties,
            Map<String, Map<String, Object>> defaults,
            boolean update) {
        Map<String, Object> emptyobject = new HashMap<>();

        for (Map.Entry<String, Map<String, Object>> propertyentry : properties.entrySet()) {

            String fieldname = propertyentry.getKey();
            emptyobject.put(fieldname, null);

            if (defaults != null && defaults.get(fieldname) != null) {
                Map<String, Object> defaultmap = defaults.get(fieldname);
                Object value = defaultmap.get("value");
                Boolean override = (Boolean) defaultmap.get("override");

                if (!update || (update && override != null && override.booleanValue())) {

                    if (String.class.isAssignableFrom(value.getClass()) && ((String) value).startsWith("$")) {
                        String dynamicvalue = (String) value;

                        if (dynamicvalue.equals("$timestamp")) {
                            value = System.currentTimeMillis();
                        } else if (dynamicvalue.equals("$loggedinuser")) {
                            value = UserContext.get("username");
                        }
                    }

                    emptyobject.put(fieldname, value);

                }
            }

        }

        return emptyobject;

    }

}
