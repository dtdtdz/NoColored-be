package com.ssafy.backend.collection.repository;

import com.ssafy.backend.collection.dao.UserCollection;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserCollectionRepository extends MongoRepository<UserCollection,String> {
}
