package com.ssafy.backend.websocket.application;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public interface TextMessageService {
    String textMessageProcessing(WebSocketSession session, TextMessage message);
}
