package com.ssafy.backend.websocket.service;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public interface MessageProcessService {
    void textMessageProcessing(WebSocketSession session, TextMessage message) throws IOException;
    void binaryMessageProcessing(WebSocketSession session, BinaryMessage message);
    void setAuthSessionTimeOut(WebSocketSession session) throws Exception;
    void setRoomQuitWarningTimeOut(WebSocketSession session);

}
