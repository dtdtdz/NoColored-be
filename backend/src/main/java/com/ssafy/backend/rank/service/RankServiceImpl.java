package com.ssafy.backend.rank.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RankServiceImpl {

    private final RedisTemplate<String, Object> redisTemplate;
    public RankServiceImpl(RedisTemplate<String, Object> redisTemplate){
        this.redisTemplate = redisTemplate;

    }
}
