package com.ssafy.backend.websocket.dao;


import com.ssafy.backend.websocket.domain.GameInfo;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@Getter
public class SessionRepository {
    //이것들 생성자 주입할까?

    public final HashMap<WebSocketSession, GameInfo> inGameUser;
    private final LinkedList<GameInfo> inGameList;
    private final Queue<GameInfo> addQueue;
    private final Queue<GameInfo> delQueue;


    //세션으로 유저찾기
    public final HashMap<WebSocketSession, UserAccessInfo> userWebsocketMap;
    // 유저로 세션찾기
//    public final static HashMap<String, UserAccessInfo> userCodeMap = new HashMap<>();
    public final HashMap<UUID, UserAccessInfo> userIdMap;
    public void removeGame(GameInfo gameInfo){
        delQueue.offer(gameInfo);
    }
    public void addGame(GameInfo gameInfo){
        addQueue.offer(gameInfo);
    }
    public void updateGameList() throws Exception{
        while (!delQueue.isEmpty()){
            inGameList.remove(delQueue.poll());
        }
        while (!addQueue.isEmpty()){
            inGameList.offer(addQueue.poll());
        }
    }
    public Iterator<GameInfo> getGameInfoIterator(){
        return inGameList.iterator();
    }
    public SessionRepository(){
        inGameUser = new HashMap<>();
        inGameList = new LinkedList<>();
        userWebsocketMap = new HashMap<>();
        userIdMap = new HashMap<>();
        addQueue = new ArrayDeque<>();
        delQueue = new ArrayDeque<>();
    }
}
