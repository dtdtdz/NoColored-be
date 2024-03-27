package com.ssafy.backend.collection.repository;

import com.ssafy.backend.collection.dao.Title;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TitleRepository extends MongoRepository<Title,Integer> {
}
