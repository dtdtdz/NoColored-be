package com.ssafy.backend.websocket.handler;


import com.ssafy.backend.websocket.service.MessageProcessService;
import com.ssafy.backend.websocket.util.SessionCollection;
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
public class MyWebSocketHandler extends AbstractWebSocketHandler {

    private final MessageProcessService messageProcessService;

    public MyWebSocketHandler(MessageProcessService messageProcessService){
        this.messageProcessService = messageProcessService;
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        // 필요에 따라 응답 보내기
//         session.sendMessage(new BinaryMessage("response".getBytes()));
//        if (binaryMessageService==null) System.out.println(11);
        messageProcessService.binaryMessageProcessing(session, message);
    }

    // 로그인용
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        messageProcessService.textMessageProcessing(session, message);
    }
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Connection established");
        messageProcessService.setAuthSessionTimeOut(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Connection closed: " + status);
//        close logic
    }


}