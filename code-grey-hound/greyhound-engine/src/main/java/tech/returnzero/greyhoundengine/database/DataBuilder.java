package tech.returnzero.greyhoundengine.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import tech.returnzero.greyhoundengine.security.UserDetailsImpl;

@Component
public class DataBuilder {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("#{'${bcrypt.fields}'.split(',')}")
    private List<String> bcryptfields;

    @Value("#{'${sensitive.fields}'.split(',')}")
    private List<String> sensitivefields;

    @Autowired
    private Environment env;

    private static final ThreadLocal<Boolean> BLOCKSENSITIVEFIELDS = new ThreadLocal<>();

    public void blocksensitives() {
        BLOCKSENSITIVEFIELDS.set(true);
    }

    public void unblocksesitives() {
        BLOCKSENSITIVEFIELDS.remove();
    }

    public boolean issensitiveblocked() {

        if (BLOCKSENSITIVEFIELDS.get() != null) {
            return BLOCKSENSITIVEFIELDS.get();
        } else {
            return false;
        }
    }

    public Object build(Map<String, Object> dataobj, String operation, String entity) throws Exception {
        return DataBuilder.class.getMethod(operation, Map.class, String.class).invoke(this, dataobj, entity);
    }

    private UserDetailsImpl userdetails() {

        Object userdata = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (UserDetailsImpl.class.equals(userdata.getClass())) {
            return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } else {
            return new UserDetailsImpl(-1l, "", "", "", new ArrayList<>(), "", "", false);
        }

    }

    public Integer create(Map<String, Object> dataobj, String entity) {

        final List<String> columns = new ArrayList<>();
        final List<String> qmarks = new ArrayList<>();

        String identitypropery = env.getProperty("security.context.id." + entity);
        String autogenerateuuid = env.getProperty("autogenerate.uuid." + entity);

        if (identitypropery != null) {
            dataobj.put(identitypropery, userdetails().getId());
        }

        if (autogenerateuuid != null) {
            dataobj.put(autogenerateuuid, UUID.randomUUID().toString());
        }

        PreparedStatementSetter preparedStatementSetter = new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                int i = 1;
                for (Map.Entry<String, Object> entry : dataobj.entrySet()) {
                    if (bcryptfields.contains(entry.getKey())) {
                        ps.setObject(i++, BCrypt.hashpw((String) entry.getValue(), BCrypt.gensalt()));
                    } else {
                        ps.setObject(i++, entry.getValue());
                    }

                }

            }
        };

        for (Map.Entry<String, Object> entry : dataobj.entrySet()) {
            columns.add(entry.getKey());
            qmarks.add("?");
        }

        String insertsql = "insert into " + entity + "( " + StringUtils.collectionToCommaDelimitedString(columns)
                + " )  values (" + StringUtils.collectionToCommaDelimitedString(qmarks) + ")";

        return jdbcTemplate.update(insertsql, preparedStatementSetter);
    }

    @SuppressWarnings("unchecked")
    public Integer update(Map<String, Object> dataobj, String entity) {

        Map<String, Object> data = (Map<String, Object>) dataobj.get("data");
        Map<String, Object> condition = (Map<String, Object>) dataobj.get("condition");
        String constraint = (String) dataobj.get("constraint");

        String identitypropery = env.getProperty("security.context.id." + entity);
        String autogenerateuuid = env.getProperty("autogenerate.uuid." + entity);

        String superadmincol = env.getProperty("superadmin.update.access.col.name" + entity);
        String superadmincolvalue = env.getProperty("superadmin.update.access.col.value" + entity);

        UserDetailsImpl details = userdetails();

        if (superadmincol != null) {
            if (!details.isSuperadmin()) {
                if (superadmincolvalue.equals(dataobj.get(superadmincol))) {
                    dataobj.put(superadmincol, superadmincolvalue);
                }
            }
        }

        if (identitypropery != null) {
            condition.put(identitypropery, new Object[] { "=", userdetails().getId() });
            data.remove(identitypropery);
        }

        if (autogenerateuuid != null) {
            data.remove(autogenerateuuid);
        }

        final List<String> columns = new ArrayList<>();

        PreparedStatementSetter preparedStatementSetter = new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                int i = 1;
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    if (bcryptfields.contains(entry.getKey())) {
                        ps.setObject(i++, BCrypt.hashpw((String) entry.getValue(), BCrypt.gensalt()));
                    } else {
                        ps.setObject(i++, entry.getValue());
                    }

                }

                for (Map.Entry<String, Object> entry : condition.entrySet()) {
                    Object[] oprvaluearr = null;
                    if (Collection.class.isAssignableFrom(entry.getValue().getClass())) {
                        oprvaluearr = ((List<Object>) entry.getValue()).toArray();
                    } else {
                        oprvaluearr = (Object[]) entry.getValue();
                    }

                    if (Collection.class.isAssignableFrom(oprvaluearr[1].getClass())) {
                        int size = ((List<Object>) oprvaluearr[1]).size();
                        for (int j = 0; j < size; j++) {
                            ps.setObject(i++, ((List<Object>) oprvaluearr[1]).get(j));
                        }
                    } else {
                        ps.setObject(i++, oprvaluearr[1]);
                    }

                }
            }
        };

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            columns.add(entry.getKey() + " = ?");
        }

        String updatequery = "update " + entity + " set " + StringUtils.collectionToCommaDelimitedString(columns)
                + " where " + prepareCondition(condition, constraint);

        return jdbcTemplate.update(updatequery, preparedStatementSetter);
    }

    private String prepareCondition(Map<String, Object> condition, String constraint) {
        return this.prepareCondition(condition, constraint, null);
    }

    @SuppressWarnings("unchecked")
    private String prepareCondition(Map<String, Object> condition, String constraint, String alias) {

        if (constraint == null) {
            constraint = "and";
        }

        if (alias == null) {
            alias = "";
        }

        final List<String> qmarks = new ArrayList<>();

        for (Map.Entry<String, Object> entry : condition.entrySet()) {
            Object[] oprvaluearr = null;

            if (Collection.class.isAssignableFrom(entry.getValue().getClass())) {
                oprvaluearr = ((List<Object>) entry.getValue()).toArray();
            } else {
                oprvaluearr = (Object[]) entry.getValue();
            }

            List<String> qmark = new ArrayList<>();
            boolean inquery = false;

            if (Collection.class.isAssignableFrom(oprvaluearr[1].getClass())) {
                int size = ((List<Object>) oprvaluearr[1]).size();
                inquery = true;
                for (int i = 0; i < size; i++) {
                    qmark.add(" ? ");
                }
            }

            String delimiterqmark = inquery ? "( " + StringUtils.collectionToCommaDelimitedString(qmark) + " )" : " ? ";

            qmarks.add(alias + entry.getKey() + " " + (String) oprvaluearr[0]
                    + delimiterqmark);
        }
        // [">=",3] , ["=", abc] , ["like", %b%]
        return StringUtils.collectionToDelimitedString(qmarks, " " + constraint + " ");

    }

    @SuppressWarnings("unchecked")
    public Integer delete(Map<String, Object> dataobj, String entity) {

        Map<String, Object> condition = (Map<String, Object>) dataobj.get("condition");
        String constraint = (String) dataobj.get("constraint");

        String identitypropery = env.getProperty("security.context.id." + entity);

        if (identitypropery != null) {
            condition.put(identitypropery, new Object[] { "=", userdetails().getId() });
        }

        PreparedStatementSetter preparedStatementSetter = new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                int i = 1;
                for (Map.Entry<String, Object> entry : condition.entrySet()) {
                    Object[] oprvaluearr = null;
                    if (Collection.class.isAssignableFrom(entry.getValue().getClass())) {
                        oprvaluearr = ((List<Object>) entry.getValue()).toArray();
                    } else {
                        oprvaluearr = (Object[]) entry.getValue();
                    }
                    if (Collection.class.isAssignableFrom(oprvaluearr[1].getClass())) {
                        int size = ((List<Object>) oprvaluearr[1]).size();
                        for (int j = 0; j < size; j++) {
                            ps.setObject(i++, ((List<Object>) oprvaluearr[1]).get(j));
                        }
                    } else {
                        ps.setObject(i++, oprvaluearr[1]);
                    }
                }
            }
        };

        String deletequery = "delete from " + entity + " where " + prepareCondition(condition, constraint);
        return jdbcTemplate.update(deletequery, preparedStatementSetter);

    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> get(Map<String, Object> dataobj, String entity) {

        Map<String, Object> condition = (Map<String, Object>) dataobj.get("condition");
        String constraint = (String) dataobj.get("constraint");

        if (condition == null) {
            condition = new HashMap<>();
        }

        String identitypropery = env.getProperty("security.context.id." + entity);

        if (identitypropery != null) {
            UserDetailsImpl details = userdetails();

            long userid = details.getId();

            if (userid != -1) {
                condition.put(identitypropery,
                        new Object[] { (details.isSuperadmin() ? "!=" : "="), userdetails().getId() });
            } else {
                condition.remove(identitypropery);
            }
        }

        List<String> columns = (List<String>) dataobj.get("columns");

        if (columns != null && !columns.isEmpty()) {
            for (int i = 0; i < columns.size(); i++) {
                if (columns.get(i).equals("id")) {
                    columns.set(i, "e." + columns.get(i));
                }

            }
        }

        Integer offset = (Integer) dataobj.get("offset");
        Integer limit = (Integer) dataobj.get("limit");
        String orderby = (String) dataobj.get("orderby");
        String order = (String) dataobj.get("order");

        List<Map<String, String>> references = (List<Map<String, String>>) dataobj.get("references");

        String joinentity = "";
        String joincolumn = "";

        if (references != null) {

            List<String> joinentities = new ArrayList<>();
            List<String> joincolumns = new ArrayList<>();

            for (Map<String, String> reference : references) {
                String alias = reference.get("alias");
                joinentities.add(reference.get("entity") + " " + alias);
                joincolumns.add(" " + alias + "." + reference.get("column") + " = e.id ");
            }

            joinentity += "," + StringUtils.collectionToCommaDelimitedString(joinentities);
            joincolumn += " where " + StringUtils.collectionToDelimitedString(joincolumns, " and ");

            if (!condition.isEmpty()) {
                joincolumn += " and ";
            }

        } else {
            if (!condition.isEmpty()) {
                joincolumn += " where ";
            }
        }

        if (offset == null) {
            offset = 0;
        }

        if (limit == null) {
            limit = 10;
        }

        List<Object> argumets = new ArrayList<>();

        for (Map.Entry<String, Object> entry : condition.entrySet()) {
            Object[] oprvaluearr = null;
            if (Collection.class.isAssignableFrom(entry.getValue().getClass())) {
                oprvaluearr = ((List<Object>) entry.getValue()).toArray();
            } else {
                oprvaluearr = (Object[]) entry.getValue();
            }
            if (Collection.class.isAssignableFrom(oprvaluearr[1].getClass())) {
                argumets.addAll((List<Object>) oprvaluearr[1]);
            } else {
                argumets.add(oprvaluearr[1]);
            }

        }

        String orderbyclause = "";

        if (order != null && orderby != null) {
            orderbyclause = " order by " + orderby + " " + order;
        }

        if (issensitiveblocked() && sensitivefields != null && !sensitivefields.isEmpty()) {
            columns.removeAll(sensitivefields);
        }

        if (!condition.isEmpty()) {
            String selectquery = "select " + StringUtils.collectionToCommaDelimitedString(columns) + " from " + entity
                    + " e"
                    + joinentity
                    + joincolumn + prepareCondition(condition, constraint) + orderbyclause
                    + " limit " + limit
                    + " offset " + offset;
            return jdbcTemplate.queryForList(selectquery, argumets.toArray());
        } else {
            String selectquery = "select " + StringUtils.collectionToCommaDelimitedString(columns) + " from " + entity
                    + " e"
                    + joinentity + joincolumn
                    + orderbyclause + " limit " + limit
                    + " offset " + offset;
            return jdbcTemplate.queryForList(selectquery);
        }

    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getUser(String attrname, Object attrvalue) {
        try {
            Map<String, Object> dataobj = new HashMap<>();
            Map<String, Object> condition = new HashMap<>();
            condition.put(attrname, new Object[] { "=", attrvalue });

            dataobj.put("condition", condition);
            dataobj.put("limit", 1);
            dataobj.put("offset", 0);

            dataobj.put("columns",
                    Arrays.asList(new String[] { "id", "username", "email", "password", "firstname", "lastname" }));

            List<Map<String, Object>> user = (List<Map<String, Object>>) this.build(dataobj, "get", "user");
            if (user != null && !user.isEmpty()) {
                return user.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
