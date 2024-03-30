package com.ssafy.backend.assets;

import com.ssafy.backend.assets.document.DatabaseSequence;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SequenceGeneratorUtil {
    private final MongoOperations mongoOperations;
    public SequenceGeneratorUtil(MongoOperations mongoOperations){
        this.mongoOperations = mongoOperations;
    }

    public long generateSequence(String seqName) {
        DatabaseSequence counter = mongoOperations.findAndModify(Query.query(Criteria.where("_id").is(seqName)),
                new Update().inc("seq",1), FindAndModifyOptions.options().returnNew(true).upsert(true),
                DatabaseSequence.class);
        return !Objects.isNull(counter) ? counter.getSeq() : 1;
    }
}
