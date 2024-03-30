package com.ssafy.backend.game.service;

import com.ssafy.backend.game.domain.GameRoomDto;
import com.ssafy.backend.game.dto.ResultDto;
import com.ssafy.backend.game.dto.UserResultDto;
import org.springframework.web.socket.WebSocketSession;

public interface GameService {
    GameRoomDto ready(String token);

    ResultDto getResult(String token);
}
