package com.ssafy.backend.websocket.util;


import com.ssafy.backend.play.domain.MatchingInfo;
import com.ssafy.backend.game.domain.GameInfo;
import com.ssafy.backend.game.domain.UserAccessInfo;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;

@Component
public class SessionCollection {
    //세션으로 유저찾기
    public final HashMap<WebSocketSession, UserAccessInfo> userWebsocketMap;
    // 유저로 세션찾기
//    public final static HashMap<String, UserAccessInfo> userCodeMap = new HashMap<>();
    public final HashMap<UUID, UserAccessInfo> userIdMap;

    public SessionCollection(){
        userWebsocketMap = new HashMap<>();
        userIdMap = new HashMap<>();


    }
}
