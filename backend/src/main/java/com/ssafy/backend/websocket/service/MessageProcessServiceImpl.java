package com.ssafy.backend.websocket.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.backend.game.domain.GameInfo;
import com.ssafy.backend.game.domain.ReceiveBinaryMessageType;
import com.ssafy.backend.game.domain.SendBinaryMessageType;
import com.ssafy.backend.game.domain.UserAccessInfo;
import com.ssafy.backend.game.util.InGameCollection;
import com.ssafy.backend.websocket.util.SessionCollection;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
public class MessageProcessServiceImpl implements MessageProcessService{

    private final SessionCollection sessionCollection;
    private final InGameCollection inGameCollection;
    private final RedisTemplate<String,Object> redisTemplate;
    private final ScheduledExecutorService authScheduledExecutorService;
    private final ObjectMapper mapper;
    private final Map<String, Function<JsonNode, Object>> actionHandlers;


    public MessageProcessServiceImpl(SessionCollection sessionCollection,
                                     InGameCollection inGameCollection,
                                     RedisTemplate<String, Object> redisTemplate,
                                     @Qualifier("authScheduledExecutorService")ScheduledExecutorService authScheduledExecutorService){
        this.sessionCollection = sessionCollection;
        this.inGameCollection = inGameCollection;
        this.redisTemplate = redisTemplate;
        this.authScheduledExecutorService = authScheduledExecutorService;
        mapper = new ObjectMapper();
        actionHandlers = new HashMap<>();
        actionHandlers.put("token", this::handleToken);
    }



    @Override
    public void textMessageProcessing(WebSocketSession session, TextMessage message) throws IOException {

        // message에서 action을 가져온다
        JsonNode jsonNode = mapper.readTree(message.getPayload());
        String action = jsonNode.get("action").asText();

        Function<JsonNode, Object> handler = actionHandlers.get(action);
//        System.out.println("text");
        if (handler != null) {
            UserAccessInfo result = (UserAccessInfo)handler.apply(jsonNode.get("token"));
            if (result!=null) {
                result.setSession(session);
                sessionCollection.userWebsocketMap.put(session, result);
                System.out.println(result.getUserProfile().getId());
            }
        } else {
            System.out.println("Unknown action: " + action);
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
            case TEST_START2 -> testStart2(session);
            case TEST_START -> testStart(session);
            case TEST_LOGIN -> testLogin(session);
        }
    }

    @Override
    public void setAuthSessionTimeOut(WebSocketSession session) throws Exception {

        authScheduledExecutorService.schedule(()->{
            if (!sessionCollection.userWebsocketMap.containsKey(session)){
                try {
                    session.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        },100, TimeUnit.SECONDS);
    }

    private UserAccessInfo handleToken(JsonNode node) {
//        System.out.println(node.asText());
        Object value = redisTemplate.opsForValue().get("token:"+node.asText());

        if (value==null) return null;
        UUID id = UUID.fromString((String) value);
        if (sessionCollection.userIdMap.containsKey(id)){
            UserAccessInfo accessInfo = sessionCollection.userIdMap.get(id);
            return accessInfo;
        }
        return null;
    }

    private void applyLeft(WebSocketSession session){
        GameInfo gameInfo = inGameCollection.inGameUser.get(session);
        int idx = gameInfo.getUsers().get(session).getCharacterNum();
        gameInfo.toLeft(idx);
//        System.out.println(0);
    }
    private void applyRight(WebSocketSession session){
        GameInfo gameInfo = inGameCollection.inGameUser.get(session);
        int idx = gameInfo.getUsers().get(session).getCharacterNum();
        gameInfo.toRight(idx);
//        System.out.println(1);
    }

    private void applyJump(WebSocketSession session){
        //바닥에 있으면 점프
        GameInfo gameInfo = inGameCollection.inGameUser.get(session);
        int idx = gameInfo.getUsers().get(session).getCharacterNum();
        gameInfo.jump(idx);
    }

    private void testStart2(WebSocketSession session) {
        GameInfo gameInfo = lastGame;

        if (gameInfo==null) return;
        gameInfo.putSession(session);
        inGameCollection.inGameUser.put(session, gameInfo);

        ByteBuffer tmpbuffer = ByteBuffer.allocate(1024);

        gameInfo.putTime(tmpbuffer);

        tmpbuffer.put(SendBinaryMessageType.TEST_MAP.getValue()).
                put((byte) 3).put((byte) gameInfo.getMapInfo().getFloorList().size());
        for (int[] arr:gameInfo.getMapInfo().getFloorList()){
            tmpbuffer.put((byte) arr[0]).put((byte) arr[1]).put((byte) arr[2]);
        }

        try {
            tmpbuffer.flip();
            synchronized (session){
                session.sendMessage(new BinaryMessage(tmpbuffer));
            }

        } catch (IOException e) {
//                    e.printStackTrace();
//                    roomInfo.getSessions().remove(session);
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (Exception e){
            System.out.println("can't find session");
            e.printStackTrace();
        }

    }
    private GameInfo lastGame;
    private void testStart(WebSocketSession session){
//        RoomInfo roomInfoEx = new RoomInfo(SessionRepository.loginUserMap(session));
        GameInfo gameInfo = new GameInfo();
        //수정 필요
        gameInfo.putSession(session);
        lastGame = gameInfo;

        ByteBuffer tmpbuffer = ByteBuffer.allocate(1024);

        gameInfo.putTime(tmpbuffer);

        tmpbuffer.put(SendBinaryMessageType.TEST_MAP.getValue()).
                put((byte) 3).put((byte) gameInfo.getMapInfo().getFloorList().size());
        for (int[] arr:gameInfo.getMapInfo().getFloorList()){
            tmpbuffer.put((byte) arr[0]).put((byte) arr[1]).put((byte) arr[2]);
        }

        try {
            tmpbuffer.flip();
            synchronized (session){
                session.sendMessage(new BinaryMessage(tmpbuffer));
            }

        } catch (IOException e) {
//                    e.printStackTrace();
//                    roomInfo.getSessions().remove(session);
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (Exception e){
            System.out.println("can't find session");
            e.printStackTrace();
        }
        inGameCollection.addGame(gameInfo);
        inGameCollection.inGameUser.put(session, gameInfo);
    }

    private void testLogin(WebSocketSession session){

    }
}
