package com.ssafy.backend.assets.dao;

import com.ssafy.backend.assets.document.DatabaseSequence;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DatabaseSequenceRepository extends MongoRepository<DatabaseSequence,String> {

}
