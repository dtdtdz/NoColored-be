package com.ssafy.backend.rank.service;

import com.ssafy.backend.rank.dto.RankDto;
import com.ssafy.backend.rank.dto.RankInfoDto;

public interface RankService {
    RankDto getRank(String token);

    RankInfoDto getRankList();
}
