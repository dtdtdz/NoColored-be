package com.ssafy.backend.rank.repository;

import com.ssafy.backend.rank.dao.RankMongo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RankRepository extends MongoRepository<RankMongo, String> {
}
