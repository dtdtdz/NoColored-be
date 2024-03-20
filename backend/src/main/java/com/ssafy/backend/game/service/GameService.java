package com.ssafy.backend.game.service;

import org.springframework.web.socket.WebSocketSession;

public interface GameService {
    void ready(String token);
}
