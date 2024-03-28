package com.ssafy.backend.rank.service;

import com.ssafy.backend.rank.dto.RankDto;
import com.ssafy.backend.rank.dto.RankInfoDto;
import org.springframework.http.ResponseEntity;

public interface RankService {
    RankDto getRank(String token);

    RankInfoDto getRankList();

    ResponseEntity<?> putRank(int dataNumber);

    ResponseEntity<?> clearRedis();

}
