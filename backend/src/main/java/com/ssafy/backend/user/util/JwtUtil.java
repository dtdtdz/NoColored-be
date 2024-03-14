package com.ssafy.backend.user.util;



import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JwtUtil {
    @Value("${jwt.secretKey}")
    private String secretKey;
    public String generateToken(String code) {
        Map<String,Object> claims = new LinkedHashMap<>();
        claims.put("user code",code);
//        System.out.println("으아악");
        long now = System.currentTimeMillis();

        String token = JWT.create()
                .withPayload(claims)
                .withIssuedAt(new Date(now))
                .withExpiresAt(new Date(now+3600*1000))
                .sign(Algorithm.HMAC256(secretKey));

        return token;
    }
}
