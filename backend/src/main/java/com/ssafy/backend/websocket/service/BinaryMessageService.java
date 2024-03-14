package com.ssafy.backend.websocket.service;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

public interface BinaryMessageService {

    void setRoom(WebSocketSession session);
    void getRoomInfoList();
    void binaryMessageProcessing(WebSocketSession session, BinaryMessage message);

}
