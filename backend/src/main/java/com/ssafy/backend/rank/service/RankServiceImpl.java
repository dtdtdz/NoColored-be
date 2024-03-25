package com.ssafy.backend.rank.service;

import com.ssafy.backend.rank.dto.RankDto;
import com.ssafy.backend.user.util.JwtUtil;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RankServiceImpl implements RankService{

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtUtil jwtUtil;

    public RankServiceImpl(RedisTemplate<String, Object> redisTemplate,
                           JwtUtil jwtUtil){
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public RankDto getRank(String token) {
        UserAccessInfo user = jwtUtil.getUserAccessInfoRedis(token);

        return new RankDto(user.getUserProfile());
    }
}
