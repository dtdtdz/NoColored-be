package com.ssafy.backend.collection.repository;

import com.ssafy.backend.collection.document.Achievement;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AchievementRepository extends MongoRepository<Achievement,Integer> {
}
