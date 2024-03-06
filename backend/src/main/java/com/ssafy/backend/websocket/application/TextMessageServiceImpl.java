package com.ssafy.backend.websocket.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Service
public class TextMessageServiceImpl implements TextMessageService{

    private final static ObjectMapper mapper = new ObjectMapper();

    @Override
    public String textMessageProcessing(WebSocketSession session, TextMessage message) {
        return null;
    }
}
