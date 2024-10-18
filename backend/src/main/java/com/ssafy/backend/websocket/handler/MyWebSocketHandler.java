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

/**
 * 웹소켓의 이진 메세지, 문자열 메세지를 분리하여 처리
 * 문자열 메세지는 유저의 토큰으로 웹소켓을 매핑하기 위해 사용
 * 이진 메세지는 60프레임으로 전송되는 게임 데이터를 경량화하기 위해 사용
 */
@Component
public class MyWebSocketHandler extends AbstractWebSocketHandler {

    private final MessageProcessService messageProcessService;

    public MyWebSocketHandler(MessageProcessService messageProcessService){
        this.messageProcessService = messageProcessService;
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
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
        messageProcessService.setRoomQuitWarningTimeOut(session);
    }


}