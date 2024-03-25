package com.ssafy.backend.rank.service;

import com.ssafy.backend.rank.dto.RankDto;

public interface RankService {
    RankDto getRank(String token);
}
