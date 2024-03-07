package com.ssafy.backend.websocket.application;

import com.ssafy.backend.websocket.dao.SessionRepository;
import com.ssafy.backend.websocket.domain.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
@Service
public class BinaryMessageServiceImpl implements BinaryMessageService {

    @Autowired
    private ScheduledExecutorService scheduledExecutorService;

    @Autowired
    private SessionRepository sessionRepository;

    private ByteBuffer[] buffer;

    @PostConstruct
    public void construct(){
        buffer = new ByteBuffer[GameInfo.MAX_PLAYER];
        for (int i=0; i<buffer.length; i++){
            buffer[i] = ByteBuffer.allocate(1024);
        }
    }
    @Override
    public void setRoom(WebSocketSession session) {

    }

    @Override
    public void getRoomInfoList() {

    }
    @EventListener(ApplicationReadyEvent.class)
    public void scheduleTaskAfterStartup() {
        long initialDelay = 0; // 시작 지연 없음
        long period = 16_666; // 17ms

        // 여기에 반복 실행할 태스크의 로직을 작성
        //            System.out.println("태스크 실행: " + System.currentTimeMillis());
        scheduledExecutorService.scheduleAtFixedRate(this::physics, initialDelay, period, TimeUnit.MICROSECONDS);
    }

    private void physics (){
//        System.out.println("p");
        for (GameInfo gameInfo : SessionRepository.inGameList){
            if (Duration.between(gameInfo.getStartTime(), LocalDateTime.now()).getSeconds()>=100){
                System.out.println("game close");
                for (Map.Entry<WebSocketSession, UserGameInfo> entry: gameInfo.getUsers().entrySet()){
                    SessionRepository.inGameUser.remove(entry.getKey());
                }

                SessionRepository.inGameList.remove(gameInfo);

            } else {
                long dt = gameInfo.tick();

                MapInfo mapInfo = gameInfo.getMapInfo();
                CharacterInfo[] characterInfoArr = gameInfo.getCharacterInfoArr();

                for (Map.Entry<WebSocketSession, UserGameInfo> entry: gameInfo.getUsers().entrySet()){
                    int bufferNum = entry.getValue().getBufferNum();
                    buffer[bufferNum].clear();
                    buffer[bufferNum].put(SendBinaryMessageType.PHYSICS_STATE.getValue())
                            .put((byte) (4*4*characterInfoArr.length))
                            .put((byte) 0).put((byte) 0);
                }
//phaser.js 에서 x좌표 이동 후 중력가속도 적용하는것처럼 작동함
                for (int i=0; i<characterInfoArr.length; i++){
                    CharacterInfo cInfo = characterInfoArr[i];
                    float tarX = cInfo.getX()+(dt/1000f)*cInfo.getVelX();
                    float halfSize = GameInfo.CHARACTER_SIZE/2f;
                    if (tarX + halfSize > mapInfo.getRight()){
                        gameInfo.toLeft(i);
                        tarX = -tarX+2*(mapInfo.getRight()-halfSize);
                    } else if (tarX - halfSize < mapInfo.getLeft()){
                        gameInfo.toRight(i);
                        tarX = 2*(mapInfo.getLeft()+halfSize)-tarX;
                    }

                    float tarY = cInfo.getY()+(dt/1000f)*cInfo.getVelY();
                    float halfSIze = GameInfo.CHARACTER_SIZE/2f;

//                    if ()
//                    o->o, o->x, x->o, x->x
//                    플랫폼위에 없으면 중력가속도 적용
//                    플랫폼위에 있다면 velY = 0;

                    cInfo.setX(tarX);
                    for (Map.Entry<WebSocketSession, UserGameInfo> entry: gameInfo.getUsers().entrySet()) {
                        int bufferNum = entry.getValue().getBufferNum();
                        buffer[bufferNum].putFloat(cInfo.getX());
                        buffer[bufferNum].putFloat(cInfo.getY());
                        buffer[bufferNum].putFloat(cInfo.getVelX());
                        buffer[bufferNum].putFloat(cInfo.getVelY());
                    }
                }

//                System.out.println("game logic");
                for (Map.Entry<WebSocketSession,UserGameInfo> entry: gameInfo.getUsers().entrySet()){
                    try {
                        int bufferNum = entry.getValue().getBufferNum();
                        buffer[bufferNum].flip();
                        entry.getKey().sendMessage(new BinaryMessage(buffer[bufferNum]));
                    } catch (IOException e) {
//                    e.printStackTrace();
//                    roomInfo.getSessions().remove(session);
                        throw new RuntimeException(e);
                    } catch (Exception e){
                        System.out.println("can't find session");
                        SessionRepository.inGameUser.remove(entry.getKey());
                        SessionRepository.inGameList.remove(gameInfo);
//                    e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void binaryMessageProcessing(WebSocketSession session, BinaryMessage message) {
        ByteBuffer byteBuffer = message.getPayload();

        // 여기에서 바이너리 데이터 처리
        byte[] arr = byteBuffer.array();
//        System.out.println("Received binary message of size: " + byteBuffer.remaining());

        ReceiveBinaryMessageType binaryMessageType = ReceiveBinaryMessageType.valueOf(arr[0]);
        if (binaryMessageType==null) return;
//        System.out.println(binaryMessageType);
        switch (binaryMessageType){
            case LEFT -> applyLeft(session);
            case RIGHT -> applyRight(session);
            case JUMP -> applyJump(session);
            case TEST_START -> testStart(session);
            case TEST_LOGIN -> testLogin(session);
        }
    }
    private void applyLeft(WebSocketSession session){
        GameInfo gameInfo = SessionRepository.inGameUser.get(session);
        int idx = gameInfo.getUsers().get(session).getCharacterNum();
        gameInfo.toLeft(idx);
//        System.out.println(0);
    }
    private void applyRight(WebSocketSession session){
        GameInfo gameInfo = SessionRepository.inGameUser.get(session);
        int idx = gameInfo.getUsers().get(session).getCharacterNum();
        gameInfo.toRight(idx);
//        System.out.println(1);
    }

    private void applyJump(WebSocketSession session){
        //바닥에 있으면 점프
        GameInfo gameInfo = SessionRepository.inGameUser.get(session);
        int idx = gameInfo.getUsers().get(session).getCharacterNum();
        gameInfo.jump(idx);
    }

    private void testStart(WebSocketSession session){
//        RoomInfo roomInfoEx = new RoomInfo(SessionRepository.loginUserMap(session));


        GameInfo gameInfo = new GameInfo();
        //수정 필요
        gameInfo.putSession(session);
        SessionRepository.inGameList.add(gameInfo);
        SessionRepository.inGameUser.put(session, gameInfo);
    }

    private void testLogin(WebSocketSession session){

    }
}
