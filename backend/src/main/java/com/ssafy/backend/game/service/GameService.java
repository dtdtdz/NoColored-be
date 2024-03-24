package com.ssafy.backend.game.service;

import com.ssafy.backend.game.domain.GameRoomDto;
import org.springframework.web.socket.WebSocketSession;

public interface GameService {
    GameRoomDto ready(String token);
}
