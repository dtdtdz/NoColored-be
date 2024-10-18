package com.ssafy.backend.websocket.util;


import com.ssafy.backend.websocket.domain.UserAccessInfo;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;

/**
 * 유저의 토큰으로 Redis에서 해당하는 유저id(UUID)를 얻고 접근
 * 유저의 웹소켓 세션으로 해당하는 유저 추적
 */
@Component
public class SessionCollection {

    public final HashMap<WebSocketSession, UserAccessInfo> userWebsocketMap;

    public final HashMap<UUID, UserAccessInfo> userIdMap;

    public SessionCollection(){
        userWebsocketMap = new HashMap<>();
        userIdMap = new HashMap<>();
    }
}
