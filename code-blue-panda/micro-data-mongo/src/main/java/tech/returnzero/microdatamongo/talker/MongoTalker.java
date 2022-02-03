package tech.returnzero.microdatamongo.talker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Indexes;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import tech.returnzero.microdatainterface.MicroDataInterface;
import tech.returnzero.microexception.MicroException;

@Component
public class MongoTalker implements MicroDataInterface {

    @Autowired
    private MongoTemplate template = null;

    private static final String OFFSET = "offset";
    private static final String LIMIT = "limit";
    private static final String SORT = "sort";
    private static final String ASC = "asc";
    private static final String REGEX = "regex";

    @Override
    @SuppressWarnings("unchecked")
    public boolean schema(Map<String,Object> sinkmap) {

        for (Entry<String,Object> sinkentry : sinkmap.entrySet()) {
            String collectionname = sinkentry.getKey();
            MongoCollection<Document> collection = template.createCollection(collectionname);

            Map<String, Object> optionmap = ( Map<String, Object>)sinkentry.getValue();
            List<String> indexes = (List<String>) optionmap.get("index");

            if (indexes != null) {
                for (String index : indexes) {
                    collection.createIndex(Indexes.ascending(index));
                }
            }
        }

        return true;
    }

    @Override
    public Object create(Map<String, Object> payload, String sinkname) throws MicroException {
        return template.insert(payload, sinkname);
    }

    @Override
    public Object get(Map<String, Object> criteria, String sinkname) throws MicroException {
        Query query = new Query();
        setQueryParams(criteria, query);
        prepareCriteria(criteria, query);
        return template.find(query, Document.class);
    }

    private void prepareCriteria(Map<String, Object> criteria, Query query) throws MicroException {
        try {
            List<Criteria> criterias = new ArrayList<>();

            for (Entry<String, Object> criteriaentry : criteria.entrySet()) {
                Criteria where = Criteria.where(criteriaentry.getKey());
                Object[] term = (Object[]) criteriaentry.getValue();
                String operator = (String) term[0];
                Object value = term[1];
                if (operator.equals(REGEX)) {
                    criterias.add((Criteria) Criteria.class.getMethod(operator, String.class).invoke(where, value));
                } else {
                    criterias.add((Criteria) Criteria.class.getMethod(operator, Object.class).invoke(where, value));
                }

            }

            query.addCriteria(new Criteria().andOperator(criterias));
        } catch (Exception e) {
            throw new MicroException(e.getMessage());
        }
    }

    private void setQueryParams(Map<String, Object> criteria, Query query) {

        Integer offset = (Integer) criteria.get(OFFSET);
        if (offset == null) {
            offset = 0;
        }

        Integer limit = (Integer) criteria.get(LIMIT);
        if (limit == null) {
            limit = 10;
        }

        String sort = (String) criteria.get(SORT);
        boolean asc = (boolean) criteria.get(ASC);

        query.skip(offset);
        query.limit(limit);
        query.addCriteria(new Criteria());

        if (sort != null) {
            query.with(Sort.by(asc ? Direction.ASC : Direction.DESC, sort));
        }

        criteria.remove(OFFSET);
        criteria.remove(LIMIT);
        criteria.remove(SORT);
        criteria.remove(ASC);
    }

    @Override
    public Integer update(Map<String, Object> payload, Map<String, Object> criteria, String sinkname)
            throws MicroException {

        Query query = new Query();
        prepareCriteria(criteria, query);
        Update update = new Update();
        for (Entry<String, Object> payloadeEntry : payload.entrySet()) {
            update.set(payloadeEntry.getKey(), payloadeEntry.getValue());
        }

        return (int) template.updateMulti(query, update, sinkname).getModifiedCount();
    }

    @Override
    public Integer delete(Map<String, Object> criteria, String sinkname) throws MicroException {
        Query query = new Query();
        prepareCriteria(criteria, query);
        return (int) template.remove(query, sinkname).getDeletedCount();
    }

    @Override
    public Object custom(String type, Map<String, Object> payload) throws MicroException {
        return null;
    }

}
