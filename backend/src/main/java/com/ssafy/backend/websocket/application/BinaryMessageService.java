package com.ssafy.backend.websocket.application;

import com.ssafy.backend.websocket.domain.GameInfo;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

public interface BinaryMessageService {

    void setRoom(WebSocketSession session);
    void getRoomInfoList();
    byte[][] calPhysics(GameInfo gameInfo);

    void binaryMessageProcessing(WebSocketSession session, BinaryMessage message);

}
