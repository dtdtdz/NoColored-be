package com.ssafy.backend.game.util;

import com.ssafy.backend.game.domain.*;
import com.ssafy.backend.game.service.GameService;
import com.ssafy.backend.play.domain.RoomInfo;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class InGameCollection {
    private final LinkedList<GameInfo> inGameList;
    private final Queue<GameInfo> addQueue;
    private final Queue<GameInfo> delQueue;

    public InGameCollection(){
        inGameList = new LinkedList<>();
        addQueue = new ConcurrentLinkedQueue<>();
        delQueue = new ConcurrentLinkedQueue<>();
    }
    public Iterator<GameInfo> getGameInfoIterator(){
        return inGameList.iterator();
    }

//    public void addGame(RoomDto roomDto){
//        GameInfo gameInfo = new GameInfo();
//        for (UserAccessInfo user:roomDto.getUserArr())
//    }
    public void addGame(RoomInfo roomInfo){
        List<UserAccessInfo> users = new LinkedList<>();
        for (UserAccessInfo user:roomInfo.getUserAccessInfos()){
            if (user != null) {
                users.add(user); // null이 아닐 경우에만 리스트에 추가
            }
        }

        users.removeIf(Objects::isNull);//null값 제거

        GameInfo gameInfo = new GameInfo(users, roomInfo.getRoomDto().getRoomId());
        for (UserAccessInfo user:users){
            user.setGameInfo(gameInfo);
        }
        addQueue.offer(gameInfo);
    }

    public void addGame(List<UserAccessInfo> users){
        GameInfo gameInfo = new GameInfo(users);
        for (UserAccessInfo user:users){
            user.setGameInfo(gameInfo);
        }
        addQueue.offer(gameInfo);
    }

    public void insertUser(WebSocketSession session, UserAccessInfo user){
        if (inGameList.isEmpty()) return;
        GameInfo gameInfo = inGameList.get((inGameList.size())-1);
        gameInfo.insertSession(session);

        user.setSession(session);
        user.setGameInfo(gameInfo);
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
