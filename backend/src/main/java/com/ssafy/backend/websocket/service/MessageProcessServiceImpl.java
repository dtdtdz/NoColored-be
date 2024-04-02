package com.ssafy.backend.websocket.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.backend.assets.SynchronizedSend;
import com.ssafy.backend.game.domain.GameInfo;
import com.ssafy.backend.play.domain.RoomInfo;
import com.ssafy.backend.play.service.FriendlyService;
import com.ssafy.backend.user.entity.UserProfile;
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

    private final FriendlyService friendlyService;


    public MessageProcessServiceImpl(SessionCollection sessionCollection,
                                     InGameCollection inGameCollection,
                                     JwtUtil jwtUtil,
                                     @Qualifier("authScheduledExecutorService")ScheduledExecutorService authScheduledExecutorService,
                                     FriendlyService friendlyService){
        this.sessionCollection = sessionCollection;
        this.inGameCollection = inGameCollection;
        this.jwtUtil = jwtUtil;
        this.authScheduledExecutorService = authScheduledExecutorService;
        mapper = new ObjectMapper();
        actionHandlers = new HashMap<>();
        actionHandlers.put("token", this::handleToken);
        this.friendlyService = friendlyService;
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
            if (result!=null) { //토큰 결과 userAccessInfo 있는가?

                if (result.getSession()!=null&&(!result.getSession().isOpen())) { //userAccessInfo 에 세션이 열려있는가?
                    //연결을 차단한다.
                    jwtUtil.deleteTokenRedis(jsonNode.get("data").asText());
                    SynchronizedSend.textSend(session, SendTextMessageType.INVALID_TOKEN.getValue(), null);
                    session.close();
                } else if (result.getSession()==null){ //세션이 없을때
                    sessionCollection.userWebsocketMap.put(session, result);
                    SynchronizedSend.textSend(session, SendTextMessageType.AUTHORIZED.getValue(), null);
                } else { //세션이 있으나 닫혀있음
                    sessionCollection.userWebsocketMap.remove(result.getSession());
                    sessionCollection.userWebsocketMap.put(session, result);
                    SynchronizedSend.textSend(session, SendTextMessageType.AUTHORIZED.getValue(), null);
                }
                result.setSession(session);
//                System.out.println(result.getUserProfile().getUserNickname());
//                System.out.println(result.getUserProfile().getId());
            } else {
                SynchronizedSend.textSend(session, SendTextMessageType.INVALID_TOKEN.getValue(), null);
                session.close();
            }
        } else {
            SynchronizedSend.textSend(session, SendTextMessageType.UNKNOWN_ACTION.getValue(), null);
            session.close();
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
            case READY -> applyReady(session);
            case DIRECTION_CHANGE -> applyDirectionChange(session);
            case JUMP -> applyJump(session);
            case TEST_START2 -> testStart2(session);
            case TEST_START -> testStart(session);
        }
    }


    @Override
    public void setAuthSessionTimeOut(WebSocketSession session) throws Exception {

        authScheduledExecutorService.schedule(()->{
            if (!sessionCollection.userWebsocketMap.containsKey(session)){
                try {
                    if (!session.isOpen()) return;
                    SynchronizedSend.textSend(session, SendTextMessageType.WEBSOCKET_TIME_OUT.getValue(), null);
                    session.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        },10, TimeUnit.SECONDS);
    }

    @Override
    public void setRoomQuitWarningTimeOut(WebSocketSession session) {
        if (!sessionCollection.userWebsocketMap.containsKey(session)) return;

        authScheduledExecutorService.schedule(()->{
            if (!sessionCollection.userWebsocketMap.containsKey(session)) return;//여전히 세션 재연결 안된상태, 재연결시 session 제거됨
            UserAccessInfo userAccessInfo = sessionCollection.userWebsocketMap.get(session);
            if (userAccessInfo.getRoomInfo()==null) return;
            setRoomQuitTimeOut(session);
        },30, TimeUnit.SECONDS);
    }

    private void setRoomQuitTimeOut(WebSocketSession session) {
        authScheduledExecutorService.schedule(()->{
            if (!sessionCollection.userWebsocketMap.containsKey(session)) return;
            UserAccessInfo userAccessInfo = sessionCollection.userWebsocketMap.get(session);
            if (userAccessInfo.getRoomInfo()==null) return;;
            friendlyService.quitRoom(userAccessInfo);
        },30, TimeUnit.SECONDS);
    }

    private UserAccessInfo handleToken(JsonNode node) {
        return jwtUtil.getUserAccessInfoRedis(node.asText());
    }

    private void applyReady(WebSocketSession session) {
        GameInfo gameInfo = sessionCollection.userWebsocketMap.get(session).getGameInfo();
        if (gameInfo==null) {
            System.out.println("can't find game");
            return;
        }
        gameInfo.getUsers().get(sessionCollection.userWebsocketMap.get(session)).setAccess(true);
    }
    private void applyDirectionChange(WebSocketSession session){

        GameInfo gameInfo = sessionCollection.userWebsocketMap.get(session).getGameInfo();
        if (gameInfo==null) {
            System.out.println("can't find game");
            return;
        }
//        System.out.println(sessionCollection.userWebsocketMap==null);
//        System.out.println(sessionCollection.userWebsocketMap.get(session)==null);
//        System.out.println(gameInfo.getUsers()==null);
//        System.out.println(gameInfo.getUsers().get(sessionCollection.userWebsocketMap.get(session))==null);
        int idx = gameInfo.getUsers().get(sessionCollection.userWebsocketMap.get(session)).getCharacterNum();
        int dir = gameInfo.getCharacterInfoArr()[idx].getDir();
        if (dir<0){
            gameInfo.toRight(idx);
        }else {
            gameInfo.toLeft(idx);
        }
//        System.out.println(0);
    }

    private void applyJump(WebSocketSession session){
        //바닥에 있으면 점프
        GameInfo gameInfo = sessionCollection.userWebsocketMap.get(session).getGameInfo();
        if (gameInfo==null) {
            System.out.println("can't find game");
            return;
        }
        int idx = gameInfo.getUsers().get(sessionCollection.userWebsocketMap.get(session)).getCharacterNum();
        gameInfo.jump(idx);
    }

    private void testStart2(WebSocketSession session) {
        UserAccessInfo user = new UserAccessInfo();
        user.setUserProfile(new UserProfile());
        user.setRoomInfo(new RoomInfo());
        sessionCollection.userWebsocketMap.put(session, user);
        inGameCollection.insertUser(session, user);
    }
    private void testStart(WebSocketSession session){
        List<UserAccessInfo> users = new ArrayList<>();
        UserAccessInfo user = new UserAccessInfo();
        user.setUserProfile(new UserProfile());
        user.setSession(session);
        user.setRoomInfo(new RoomInfo());
        users.add(user);
        sessionCollection.userWebsocketMap.put(session, user);
        inGameCollection.addGame(users);
    }
}
