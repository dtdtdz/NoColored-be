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
    private Map<WebSocketSession, Integer> sessions = new LinkedHashMap();
    private MapInfo mapInfo;
    private CharacterInfo[] characterInfoArr;
    private float[][] characters;//0: x, 1: y, 2: velx, 3: vely


    //이거 둘다 위치 바꿔야하나?
    private int characterSize = 48;
    private float DEFAULT_SPEED = 160;

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
        characters = new float[2][4];
        for (int i=0; i<1; i++){
            characters[i][0] = 200;
            characters[i][2] = DEFAULT_SPEED;
        }
        mapInfo = new MapInfo();
    }
    public GameInfo(int num){
        startTime = LocalDateTime.now();
        time = System.currentTimeMillis();
    }

    public long tick(){
        long now = System.currentTimeMillis();
        long result = now - time;
        time = now;
        return result;
    }

    //세션과 캐릭터를 매핑한다.
    public void putSession(WebSocketSession session){
        sessions.put(session,sessions.size());
    }

    //사용 안하나?
    public void delSession(WebSocketSession session){
        sessions.remove(session);
    }

}
