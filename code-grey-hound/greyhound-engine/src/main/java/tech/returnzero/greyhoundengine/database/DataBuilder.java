package tech.returnzero.greyhoundengine.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DataBuilder {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("#{'${bcrypt.fields}'.split(',')}")
    private List<String> bcryptfields;

    public Object build(Map<String, Object> dataobj, String operation, String entity) throws Exception {
        return DataBuilder.class.getMethod(operation, Map.class, String.class).invoke(this, dataobj, entity);
    }

    public Integer create(Map<String, Object> dataobj, String entity) {

        final List<String> columns = new ArrayList<>();
        final List<String> qmarks = new ArrayList<>();
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
                    ps.setObject(i++, oprvaluearr[1]);
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

    @SuppressWarnings("unchecked")
    private String prepareCondition(Map<String, Object> condition, String constraint) {

        if (constraint == null) {
            constraint = "and";
        }

        final List<String> qmarks = new ArrayList<>();
        for (Map.Entry<String, Object> entry : condition.entrySet()) {
            Object[] oprvaluearr = null;

            if (Collection.class.isAssignableFrom(entry.getValue().getClass())) {
                oprvaluearr = ((List<Object>) entry.getValue()).toArray();
            } else {
                oprvaluearr = (Object[]) entry.getValue();
            }
            qmarks.add(entry.getKey() + " " + (String) oprvaluearr[0] + " ?");
        }
        // [">=",3] , ["=", abc] , ["like", %b%]
        return StringUtils.collectionToDelimitedString(qmarks, ", " + constraint + " ");

    }

    @SuppressWarnings("unchecked")
    public Integer delete(Map<String, Object> dataobj, String entity) {
        Map<String, Object> condition = (Map<String, Object>) dataobj.get("condition");
        String constraint = (String) dataobj.get("constraint");

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
                    ps.setObject(i++, oprvaluearr[1]);
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

        List<String> columns = (List<String>) dataobj.get("columns");

        Integer offset = (Integer) dataobj.get("offset");
        Integer limit = (Integer) dataobj.get("limit");
        String orderby = (String) dataobj.get("orderby");
        String order = (String) dataobj.get("order");

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
            argumets.add(oprvaluearr[1]);
        }

        String orderbyclause = "";

        if (order != null && orderby != null) {
            orderbyclause = " order by " + orderby + " " + order;
        }
        if (!condition.isEmpty()) {
            String selectquery = "select " + StringUtils.collectionToCommaDelimitedString(columns) + " from " + entity
                    + " where " + prepareCondition(condition, constraint) + orderbyclause + " limit " + limit
                    + " offset " + offset;
            return jdbcTemplate.queryForList(selectquery, argumets.toArray());
        } else {
            String selectquery = "select " + StringUtils.collectionToCommaDelimitedString(columns) + " from " + entity
                    + orderbyclause + " limit " + limit
                    + " offset " + offset;
            return jdbcTemplate.queryForList(selectquery);
        }

    }

}
