package com.ssafy.backend.collection.repository;

import com.ssafy.backend.collection.document.Label;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LabelRepository extends MongoRepository<Label,Integer> {
}
