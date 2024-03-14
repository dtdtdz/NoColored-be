package com.ssafy.backend.game.util;

import com.ssafy.backend.game.domain.GameInfo;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;

@Component
public class InGameCollection {
    private final LinkedList<GameInfo> inGameList;
    private final Queue<GameInfo> addQueue;
    private final Queue<GameInfo> delQueue;
    public final HashMap<WebSocketSession, GameInfo> inGameUser;
    public Iterator<GameInfo> getGameInfoIterator(){
        return inGameList.iterator();
    }

    public synchronized void removeGame(GameInfo gameInfo){
        delQueue.offer(gameInfo);
    }
    public synchronized void addGame(GameInfo gameInfo){
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

    public InGameCollection(){
        inGameList = new LinkedList<>();
        addQueue = new ArrayDeque<>();
        delQueue = new ArrayDeque<>();
        inGameUser = new HashMap<>();
    }
}
