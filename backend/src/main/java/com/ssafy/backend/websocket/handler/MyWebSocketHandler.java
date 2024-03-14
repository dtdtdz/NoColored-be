package com.ssafy.backend.websocket.handler;


import com.ssafy.backend.websocket.service.BinaryMessageService;
import com.ssafy.backend.websocket.service.TextMessageService;
import com.ssafy.backend.websocket.dao.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class MyWebSocketHandler extends AbstractWebSocketHandler {

    private final BinaryMessageService binaryMessageService;
    private final TextMessageService textMessageService;
    private final SessionRepository sessionRepository;
    private final ScheduledExecutorService authScheduledExecutorService;

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        // 필요에 따라 응답 보내기
//         session.sendMessage(new BinaryMessage("response".getBytes()));
//        if (binaryMessageService==null) System.out.println(11);
        binaryMessageService.binaryMessageProcessing(session, message);
    }

    // 로그인용
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        textMessageService.textMessageProcessing(session, message);
    }
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Connection established");

        authScheduledExecutorService.schedule(()->{
            if (!sessionRepository.userWebsocketMap.containsKey(session)){
                try {
                    session.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        },1000, TimeUnit.SECONDS);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Connection closed: " + status);
//        close logic
    }


}