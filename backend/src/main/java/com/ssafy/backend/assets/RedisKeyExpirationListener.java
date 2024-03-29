package com.ssafy.backend.assets;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class RedisKeyExpirationListener implements MessageListener {

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        System.out.println("Expired key: " + expiredKey);
    }
}
