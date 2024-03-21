package com.ssafy.backend.game.util;

import com.ssafy.backend.assets.SynchronizedSend;
import com.ssafy.backend.game.domain.*;
import com.ssafy.backend.websocket.domain.SendBinaryMessageType;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class InGameCollection {
    private final LinkedList<GameInfo> inGameList;
    private final Queue<GameInfo> addQueue;
    private final Queue<GameInfo> delQueue;
    public final HashMap<WebSocketSession, GameInfo> inGameUser;

    public InGameCollection(){
        inGameList = new LinkedList<>();
        addQueue = new ConcurrentLinkedQueue<>();
        delQueue = new ConcurrentLinkedQueue<>();
        inGameUser = new HashMap<>();
    }
    public Iterator<GameInfo> getGameInfoIterator(){
        return inGameList.iterator();
    }

//    public void addGame(RoomDto roomDto){
//        GameInfo gameInfo = new GameInfo();
//        for (UserAccessInfo user:roomDto.getUserArr())
//    }
    public void addGame(List<UserAccessInfo> users){
        GameInfo gameInfo = new GameInfo(users);
        for (UserAccessInfo user:users){
            inGameUser.put(user.getSession(), gameInfo);
            user.setGameInfo(gameInfo);
        }
//        gameInfo.putTime();
//        gameInfo.putTestMap();

        gameInfo.sendBuffer();
        addQueue.offer(gameInfo);
    }

    public void insertUser(WebSocketSession session){
        if (inGameList.isEmpty()) return;
        GameInfo gameInfo = inGameList.get((inGameList.size())-1);
        gameInfo.insertSession(session);
        inGameUser.put(session, gameInfo);
    }

    public void removeGame(GameInfo gameInfo){
        delQueue.offer(gameInfo);
    }
    public void updateGameList() throws Exception{
        while (!delQueue.isEmpty()){
            inGameList.remove(delQueue.poll());
        }
        while (!addQueue.isEmpty()){
            inGameList.offer(addQueue.poll());
        }
    }




}
