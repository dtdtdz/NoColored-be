package com.ssafy.backend.user.util;



import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
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
        redisTemplate.opsForValue().set("test",1,10,TimeUnit.SECONDS);
        redisTemplate.expire("test",5,TimeUnit.SECONDS);
    }

    public String generateToken(String code) {
        Map<String,Object> claims = new LinkedHashMap<>();
        claims.put("user code",code);

        long now = System.currentTimeMillis();

        String token = JWT.create()
                .withPayload(claims)
                .withIssuedAt(new Date(now))
                .withExpiresAt(new Date(now+1800*1000))
                .sign(Algorithm.HMAC256(secretKey));

        return token;
    }

    public void setTokenRedis(String token, UUID id){
//        if (redisTemplate.opsForValue().get(tokenKey(token)) !=null) System.out.println("dup");
        synchronized (redisTemplate){
            redisTemplate.opsForValue().set(tokenKey(token), id, 1800, TimeUnit.SECONDS);//30분 살아있음
        }
    }

    public void deleteTokenRedis(String token){
        synchronized (redisTemplate){
            redisTemplate.delete(tokenKey(token));
        }
    }

    public UserAccessInfo getUserAccessInfoRedis(String token){
        if (token==null|| token.isEmpty()) throw new RuntimeException("Missing token");
        Object value = null;
        synchronized (redisTemplate){
            value = redisTemplate.opsForValue().get(tokenKey(token));
        }

        if (value==null) {
            System.out.println("Token is invalid: "+token);
            throw new RuntimeException("Token is invalid");
        }
        redisTemplate.expire(tokenKey(token),1800, TimeUnit.SECONDS);
        UUID id = UUID.fromString((String) value);
        if (sessionCollection.userIdMap.containsKey(id)){
            UserAccessInfo userAccessInfo = sessionCollection.userIdMap.get(id);
            if (userAccessInfo==null) return null;
            userAccessInfo.setExpireTime();
            return userAccessInfo;
        }
        return null;
    }

    private String tokenKey(String token){
        return "token:"+token;
    }
}
