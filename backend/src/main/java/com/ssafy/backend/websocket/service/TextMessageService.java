package com.ssafy.backend.websocket.service;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public interface TextMessageService {
    String textMessageProcessing(WebSocketSession session, TextMessage message) throws IOException;
}
