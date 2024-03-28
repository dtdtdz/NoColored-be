package com.ssafy.backend.rank.service;

import com.ssafy.backend.rank.dto.RankDto;
import com.ssafy.backend.rank.dto.RankInfoDto;
import com.ssafy.backend.user.dto.UserProfileDto;
import org.springframework.http.ResponseEntity;

public interface RankService {
    UserProfileDto getRank(String token);

    RankInfoDto getRankList();

    ResponseEntity<?> putRank(int dataNumber);

    ResponseEntity<?> clearRedis();

}
