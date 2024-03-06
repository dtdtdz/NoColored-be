package com.ssafy.backend.websocket.application;

import com.ssafy.backend.websocket.dao.SessionRepository;
import com.ssafy.backend.websocket.domain.MapInfo;
import com.ssafy.backend.websocket.domain.GameInfo;
import com.ssafy.backend.websocket.domain.ReceiveBinaryMessageType;
import com.ssafy.backend.websocket.domain.SendBinaryMessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class BinaryMessageServiceImpl implements BinaryMessageService {


    @Autowired
    private SessionRepository sessionRepository;

    private ByteBuffer buffer = ByteBuffer.allocate(256);
    @Override
    public void setRoom(WebSocketSession session) {

    }

    @Override
    public void getRoomInfoList() {

    }

    @Override
    public byte[][] calPhysics(GameInfo gameInfo) {
        if (Duration.between(gameInfo.getStartTime(), LocalDateTime.now()).getSeconds()>=100){
            System.out.println("game close");
            for (Map.Entry<WebSocketSession, Integer> entry: gameInfo.getSessions().entrySet()){
                SessionRepository.inGameUser.remove(entry.getKey());
            }

            SessionRepository.inGameList.remove(gameInfo);

        } else {
            long dt = gameInfo.tick();

            MapInfo mapInfo = gameInfo.getMapInfo();
            buffer.clear();
            buffer.put(SendBinaryMessageType.PHYSICS_STATE.getValue()).
                    put((byte) (4*2)).
                    put((byte) 0).put((byte) 0);
            for (float[] posvel : gameInfo.getCharacters()){
                float tarx = posvel[0]+(dt/1000f)*posvel[2];
                float halfSize = gameInfo.getCharacterSize()/2f;
                if (tarx + halfSize > mapInfo.getRight()){
                    posvel[2] = -Math.abs(posvel[2]);
                    tarx = -tarx+2*(mapInfo.getRight()-halfSize);
                } else if ( tarx - halfSize < mapInfo.getLeft()){
                    posvel[2] = Math.abs(posvel[2]);
                    tarx = 2*(mapInfo.getLeft()+halfSize)-tarx;
                }

                posvel[0] = tarx;
                buffer.putFloat(tarx);
                System.out.println(tarx);
//                System.out.println(tarx +":"+ dt +":"+dt+":"+(dt*posvel[2]/1000));
            }

//                System.out.println("game logic");
            for (Map.Entry<WebSocketSession,Integer> entry: gameInfo.getSessions().entrySet()){
                try {
                    //UserInfo?
                    buffer.flip();
                    entry.getKey().sendMessage(new BinaryMessage(buffer));
                } catch (IOException e) {
//                    e.printStackTrace();
//                    roomInfo.getSessions().remove(session);
                    throw new RuntimeException(e);
                } catch (Exception e){
                    SessionRepository.inGameUser.remove(entry.getKey());
                    SessionRepository.inGameList.remove(gameInfo);
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Scheduled(fixedRate = 15)
    private void physics (){
//        System.out.println("p");
        for (GameInfo gameInfo : SessionRepository.inGameList){
            byte[][] bytes = calPhysics(gameInfo);
//            for (int i=0; i< bytes.length; i++){
//                try {
//                    roomInfo.getSessions().k(new BinaryMessage(bytes[i]));
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
        }
    }

    @Override
    public void binaryMessageProcessing(WebSocketSession session, BinaryMessage message) {
        ByteBuffer byteBuffer = message.getPayload();

        // 여기에서 바이너리 데이터 처리
        byte[] arr = byteBuffer.array();
        System.out.println("Received binary message of size: " + byteBuffer.remaining());

        ReceiveBinaryMessageType binaryMessageType = ReceiveBinaryMessageType.valueOf(arr[0]);
        if (binaryMessageType==null) return;
        switch (binaryMessageType){
            case RECEIVE_JUMP -> applyJump(session);
            case RECEIVE_DIRECTION -> applyDirectionChange(session);
            case TEST_START -> testStart(session);
            case TEST_LOGIN -> testLogin(session);
        }

    }

    private void applyDirectionChange(WebSocketSession session){
        //반대 방향으로 변경
    }

    private void applyJump(WebSocketSession session){
        //바닥에 있으면 점프
        GameInfo gameInfo = SessionRepository.inGameUser.get(session);

    }

    private void testStart(WebSocketSession session){
        GameInfo gameInfo = new GameInfo();
        gameInfo.putSession(session);
        SessionRepository.inGameList.add(gameInfo);
        SessionRepository.inGameUser.put(session, gameInfo);
    }

    private void testLogin(WebSocketSession session){

    }
}
