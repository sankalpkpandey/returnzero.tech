package tech.returnzero.microdatajdbc.talker;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.util.StringUtils;

import tech.returnzero.microdatainterface.MicroDataInterface;
import tech.returnzero.microexception.MicroException;

public class JDBCTalker implements MicroDataInterface {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Object create(Map<String, Object> payload, String sinkname) throws MicroException {

        final List<String> columns = new ArrayList<>();
        final List<String> qmarks = new ArrayList<>();

        PreparedStatementSetter preparedStatementSetter = new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                int i = 1;
                for (Map.Entry<String, Object> entry : payload.entrySet()) {
                    ps.setObject(i++, entry.getValue());

                }

            }
        };

        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            columns.add(entry.getKey());
            qmarks.add("?");
        }

        String insertsql = "insert into " + sinkname + "( " + StringUtils.collectionToCommaDelimitedString(columns)
                + " )  values (" + StringUtils.collectionToCommaDelimitedString(qmarks) + ")";

        return jdbcTemplate.update(insertsql, preparedStatementSetter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object get(Map<String, Object> criteria, String sinkname) throws MicroException {

        Map<String, Object> condition = (Map<String, Object>) criteria.get("condition");
        String constraint = null;

        if (condition == null) {
            condition = new HashMap<>();
        }

        List<String> columns = (List<String>) criteria.get("columns");

        Integer offset = (Integer) criteria.get("offset");
        Integer limit = (Integer) criteria.get("limit");
        String orderby = (String) criteria.get("orderby");
        String order = (String) criteria.get("order");

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

        if (!condition.isEmpty()) {
            String selectquery = "select " + StringUtils.collectionToCommaDelimitedString(columns) + " from " + sinkname
                    + " where " + prepareCondition(condition, constraint) + orderbyclause + " limit " + limit
                    + " offset " + offset;
            return jdbcTemplate.queryForList(selectquery, argumets.toArray());
        } else {
            String selectquery = "select " + StringUtils.collectionToCommaDelimitedString(columns) + " from " + sinkname
                    + orderbyclause + " limit " + limit
                    + " offset " + offset;
            return jdbcTemplate.queryForList(selectquery);
        }

    }

    @Override
    @SuppressWarnings("unchecked")
    public Integer update(Map<String, Object> payload, Map<String, Object> criteria, String sinkname)
            throws MicroException {

        String constraint = null;

        final List<String> columns = new ArrayList<>();

        PreparedStatementSetter preparedStatementSetter = new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                int i = 1;
                for (Map.Entry<String, Object> entry : payload.entrySet()) {

                    ps.setObject(i++, entry.getValue());

                }

                for (Map.Entry<String, Object> entry : criteria.entrySet()) {
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

        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            columns.add(entry.getKey() + " = ?");
        }

        String updatequery = "update " + sinkname + " set " + StringUtils.collectionToCommaDelimitedString(columns)
                + " where " + prepareCondition(criteria, constraint);

        return jdbcTemplate.update(updatequery, preparedStatementSetter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Integer delete(Map<String, Object> criteria, String sinkname) throws MicroException {
        String constraint = null;

        PreparedStatementSetter preparedStatementSetter = new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                int i = 1;
                for (Map.Entry<String, Object> entry : criteria.entrySet()) {
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

        String deletequery = "delete from " + sinkname + " where " + prepareCondition(criteria, constraint);
        return jdbcTemplate.update(deletequery, preparedStatementSetter);
    }

    @Override
    public Object custom(String type, Map<String, Object> payload) throws MicroException {
        return null;
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

            qmarks.add(entry.getKey() + " " + (String) oprvaluearr[0]
                    + delimiterqmark);
        }
        // [">=",3] , ["=", abc] , ["like", %b%]
        return StringUtils.collectionToDelimitedString(qmarks, " " + constraint + " ");

    }

    @Override
    public boolean schema(Map<String, Object> sinkmap) {
        // TODO Auto-generated method stub
        return false;
    }

}
