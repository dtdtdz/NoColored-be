package com.ssafy.backend.websocket.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;


@Getter
@AllArgsConstructor
public class GameInfo {
    private LocalDateTime startTime;
    private long time;
    private Map<WebSocketSession, UserGameInfo> users = new LinkedHashMap<>();
    private MapInfo mapInfo;
    private CharacterInfo[] characterInfoArr;

    //이거 둘다 위치 바꿔야하나?
    private int characterSize = 36;
    public static final float DEFAULT_SPEED = 160;
    public static final int MAX_PLAYER = 4;
    int roomId;
    public GameInfo(){
        startTime = LocalDateTime.now();
        time = System.currentTimeMillis();
        characterInfoArr = new CharacterInfo[3];
        for (int i=0; i<characterInfoArr.length; i++){
            characterInfoArr[i] = new CharacterInfo();
            characterInfoArr[i].setX((1+i)*100);
            characterInfoArr[i].setVelX(DEFAULT_SPEED);
        }

        mapInfo = new MapInfo();
    }

    public GameInfo(int num){
        startTime = LocalDateTime.now();
        time = System.currentTimeMillis();
    }

    public void toLeft(int idx){
        characterInfoArr[idx].setVelX(-Math.abs(characterInfoArr[idx].getVelX()));
    }

    public void toRight(int idx){
        characterInfoArr[idx].setVelX(Math.abs(characterInfoArr[idx].getVelX()));
    }

    public void jump(int idx){
        characterInfoArr[idx].setVelY(-300);
    }

    public long tick(){
        long now = System.currentTimeMillis();
        long result = now - time;
        time = now;
        return result;
    }

    //세션과 캐릭터를 매핑한다.
    public void putSession(WebSocketSession session){
        users.put(session, new UserGameInfo(session, users.size(), users.size()));
    }

    //사용 안하나?
    public void delSession(WebSocketSession session){
        users.remove(session);
    }

}
