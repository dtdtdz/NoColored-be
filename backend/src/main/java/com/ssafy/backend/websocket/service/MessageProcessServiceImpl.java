package com.ssafy.backend.websocket.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.backend.assets.SynchronizedSend;
import com.ssafy.backend.game.domain.GameInfo;
import com.ssafy.backend.websocket.domain.ReceiveBinaryMessageType;
import com.ssafy.backend.websocket.domain.SendTextMessageType;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import com.ssafy.backend.game.util.InGameCollection;
import com.ssafy.backend.user.util.JwtUtil;
import com.ssafy.backend.websocket.util.SessionCollection;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
public class MessageProcessServiceImpl implements MessageProcessService{

    private final SessionCollection sessionCollection;
    private final InGameCollection inGameCollection;
    private final JwtUtil jwtUtil;
    private final ScheduledExecutorService authScheduledExecutorService;
    private final ObjectMapper mapper;
    private final Map<String, Function<JsonNode, Object>> actionHandlers;


    public MessageProcessServiceImpl(SessionCollection sessionCollection,
                                     InGameCollection inGameCollection,
                                     JwtUtil jwtUtil,
                                     @Qualifier("authScheduledExecutorService")ScheduledExecutorService authScheduledExecutorService){
        this.sessionCollection = sessionCollection;
        this.inGameCollection = inGameCollection;
        this.jwtUtil = jwtUtil;
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
            UserAccessInfo result = (UserAccessInfo)handler.apply(jsonNode.get("data"));
            if (result!=null) {
                result.setSession(session);
                sessionCollection.userWebsocketMap.put(session, result);
                SynchronizedSend.textSend(session, "authorization",null);
//                System.out.println(result.getUserProfile().getUserNickname());
//                System.out.println(result.getUserProfile().getId());
            } else {
                SynchronizedSend.textSend(session, "invalidToken", null);
            }
        } else {
            SynchronizedSend.textSend(session, "unknownAction", null);
//            System.out.println("Unknown action: " + action);
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
                    SynchronizedSend.textSend(session, SendTextMessageType.WEBSOCKET_TIME_OUT.getValue(), null);
                    session.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        },10, TimeUnit.SECONDS);
    }

    private UserAccessInfo handleToken(JsonNode node) {
        return jwtUtil.getUserAccessInfoRedis(node.asText());
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
        inGameCollection.insertUser(session);
    }
    private void testStart(WebSocketSession session){
        List<UserAccessInfo> users = new ArrayList<>();
        UserAccessInfo user = new UserAccessInfo();
        user.setSession(session);
        users.add(user);
        inGameCollection.addGame(users);
    }

    private void testLogin(WebSocketSession session){

    }
}
