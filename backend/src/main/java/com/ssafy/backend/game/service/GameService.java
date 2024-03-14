package com.ssafy.backend.game.service;

import org.springframework.web.socket.WebSocketSession;

public interface GameService {

    void setRoom(WebSocketSession session);
    void getRoomInfoList();

}
