package com.ssafy.backend.collection.repository;

import com.ssafy.backend.collection.document.UserCollection;
import com.ssafy.backend.user.entity.UserAchievements;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserCollectionRepository extends MongoRepository<UserCollection,String> {
    UserCollection findByUserCode(String userCode);
}
