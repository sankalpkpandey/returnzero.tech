package tech.returnzero.microdatacassandra.talker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.util.StringUtils;

import tech.returnzero.microdatainterface.MicroDataInterface;
import tech.returnzero.microexception.MicroException;

public class CassandraTalker implements MicroDataInterface {

    /**
     * https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#application-properties.data.spring.data.cassandra.compression
     */
    @Autowired
    private CqlTemplate cqltemplate;

    @Override
    public Object create(Map<String, Object> payload, String sinkname) throws MicroException {

        final List<String> columns = new ArrayList<>();
        final List<String> qmarks = new ArrayList<>();
        final List<Object> parameters = new ArrayList<>();

        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            parameters.add(entry.getValue());
        }

        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            columns.add(entry.getKey());
            qmarks.add("?");
        }

        String insertsql = "insert into " + sinkname + "( " + StringUtils.collectionToCommaDelimitedString(columns)
                + " )  values (" + StringUtils.collectionToCommaDelimitedString(qmarks) + ")";
        return cqltemplate.execute(insertsql, parameters.toArray());
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
            return cqltemplate.queryForList(selectquery, argumets.toArray());
        } else {
            String selectquery = "select " + StringUtils.collectionToCommaDelimitedString(columns) + " from " + sinkname
                    + orderbyclause + " limit " + limit
                    + " offset " + offset;
            return cqltemplate.queryForList(selectquery);
        }

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
    @SuppressWarnings("unchecked")
    public Integer update(Map<String, Object> payload, Map<String, Object> criteria, String sinkname)
            throws MicroException {
        String constraint = null;

        final List<String> columns = new ArrayList<>();
        List<Object> argumets = new ArrayList<>();

        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            argumets.add(entry.getValue());
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
                    argumets.add(((List<Object>) oprvaluearr[1]).get(j));
                }
            } else {
                argumets.add(oprvaluearr[1]);
            }

        }

        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            columns.add(entry.getKey() + " = ?");
        }

        String updatequery = "update " + sinkname + " set " + StringUtils.collectionToCommaDelimitedString(columns)
                + " where " + prepareCondition(criteria, constraint);

        boolean executed = cqltemplate.execute(updatequery, argumets.toArray());

        return executed ? 0 : 1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Integer delete(Map<String, Object> criteria, String sinkname) throws MicroException {

        String constraint = null;
        List<Object> argumets = new ArrayList<>();

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
                    argumets.add(((List<Object>) oprvaluearr[1]).get(j));
                }
            } else {
                argumets.add(oprvaluearr[1]);
            }
        }

        String deletequery = "delete from " + sinkname + " where " + prepareCondition(criteria, constraint);
        boolean executed = cqltemplate.execute(deletequery, argumets.toArray());

        return executed ? 0 : 1;
    }

    @Override
    public Object custom(String type, Map<String, Object> payload) throws MicroException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean schema(Map<String, Object> sinkmap) {
        // TODO Auto-generated method stub
        return false;
    }

}
