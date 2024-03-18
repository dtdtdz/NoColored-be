package com.ssafy.backend.game.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Getter
@AllArgsConstructor
public class GameInfo {
    private LocalDateTime startDate;
    private long startTime;
    private long time;
    private int second;
    private Map<WebSocketSession, UserGameInfo> users = new LinkedHashMap<>();
    private MapInfo mapInfo;
    private CharacterInfo[] characterInfoArr;
    private boolean[][] floor;
    private List<byte[]> stepList;
    //이것들 위치 바꿔야하나?
    public static final int CHARACTER_SIZE = 27;
    public static final float DEFAULT_SPEED = 160;
    public static final int MAX_PLAYER = 4;
    public static final float GRAVITY = 200;
    public static final int BLOCK_SIZE = 18;
    public static final int MAP_HEIGHT = 19;
    public static final int MAP_WIDTH = 27;
    public static final int WALL_WIDTH = 3;
    public static final int DEFAULT_TIME = 120;
    public static final int CHARACTER_NUM = 10;

    int roomId;
    public GameInfo(){
        startDate = LocalDateTime.now();
        startTime = System.currentTimeMillis();
        time = startTime;
        second = DEFAULT_TIME;
        characterInfoArr = new CharacterInfo[CHARACTER_NUM];
        for (int i=0; i<characterInfoArr.length; i++){
            characterInfoArr[i] = new CharacterInfo();
            characterInfoArr[i].setX((1+i)*100);
            characterInfoArr[i].setY(0);
            characterInfoArr[i].setVelX(DEFAULT_SPEED);
        }

        mapInfo = new MapInfo();
        floor = new boolean[MAP_WIDTH][MAP_HEIGHT];
        List<int[]> list = mapInfo.getFloorList();
        for (int[] arr:list){
            for (int i=0; i<arr[2]; i++){
                floor[arr[0]+i-WALL_WIDTH][arr[1]] = true;
            }
        }
        stepList = new ArrayList<>();
    }

    private GameInfo(int num){
        startDate = LocalDateTime.now();
        startTime = System.currentTimeMillis();
        time = startTime;
        second = DEFAULT_TIME;
        characterInfoArr = new CharacterInfo[3];
        for (int i=0; i<characterInfoArr.length; i++){
            characterInfoArr[i] = new CharacterInfo();
            characterInfoArr[i].setX((1+i)*100);
            characterInfoArr[i].setY(0);
            characterInfoArr[i].setVelX(DEFAULT_SPEED);
        }

        mapInfo = new MapInfo();//num
        floor = new boolean[MAP_HEIGHT][MAP_WIDTH];
    }

    public void toLeft(int idx){
        characterInfoArr[idx].setVelX(-Math.abs(characterInfoArr[idx].getVelX()));
    }

    public void toRight(int idx){
        characterInfoArr[idx].setVelX(Math.abs(characterInfoArr[idx].getVelX()));
    }

    public void jump(int idx){
        characterInfoArr[idx].setJump(true);
    }

    public long tick(){
        long now = System.currentTimeMillis();
        long result = now - time;
        time = now;

        return result;
    }

    public boolean checkSecond(){
        int newSecond = DEFAULT_TIME-(int)((time-startTime)/1000);
        if (newSecond<second){
            second = newSecond;
            return true;
        }
        return false;
    }

    public void putStep(ByteBuffer buffer){
        buffer.put(SendBinaryMessageType.STEP.getValue())
                .put((byte) 3).put((byte) stepList.size());
        for (int i=0; i<stepList.size(); i++){
            buffer.put(stepList.get(i)[0]).put(stepList.get(i)[1]).put(stepList.get(i)[2]);
        }
    }

    public void putTime(ByteBuffer buffer){
        buffer.put(SendBinaryMessageType.TIME.getValue())
                .put((byte) 1).put((byte)1).put((byte)second);
    }

    //세션과 캐릭터를 매핑한다.
    public void putSession(WebSocketSession session){
        byte num = (byte) users.size();
        UserGameInfo user = new UserGameInfo(session, num, num, num, (byte) 0);
        users.put(session, user);
        characterInfoArr[num].setUserGameInfo(user);
    }

    //사용 안하나?
    public void delSession(WebSocketSession session){
        users.remove(session);
    }

}
