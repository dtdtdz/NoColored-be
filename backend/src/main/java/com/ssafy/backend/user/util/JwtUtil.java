package com.ssafy.backend.user.util;



import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.ssafy.backend.game.domain.UserAccessInfo;
import com.ssafy.backend.websocket.util.SessionCollection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {
    @Value("${jwt.secretKey}")
    private String secretKey;
    private final SessionCollection sessionCollection;
    private final RedisTemplate<String, Object> redisTemplate;
    public JwtUtil(SessionCollection sessionCollection,
            RedisTemplate<String,Object> redisTemplate){
        this.sessionCollection = sessionCollection;
        this.redisTemplate = redisTemplate;
    }

    public String generateToken(String code) {
        Map<String,Object> claims = new LinkedHashMap<>();
        claims.put("user code",code);

        long now = System.currentTimeMillis();

        String token = JWT.create()
                .withPayload(claims)
                .withIssuedAt(new Date(now))
                .withExpiresAt(new Date(now+3600*1000))
                .sign(Algorithm.HMAC256(secretKey));

        return token;
    }

    public void setTokenRedis(String token, UUID id){
        redisTemplate.opsForValue().set(tokenKey(token), id, 3600*8, TimeUnit.SECONDS);//8시간 살아있음
    }

    public void deleteTokenRedis(String token){
        redisTemplate.delete(tokenKey(token));
    }

    public UserAccessInfo getUserAccessInfoRedis(String token){
        //        System.out.println(node.asText());
        Object value = redisTemplate.opsForValue().get(tokenKey(token));

        if (value==null) return null;
        UUID id = UUID.fromString((String) value);
        if (sessionCollection.userIdMap.containsKey(id)){
            return sessionCollection.userIdMap.get(id);
        }
        return null;
    }

    private String tokenKey(String token){
        return "token:"+token;
    }
}
