package com.ssafy.backend.play.service;

import com.ssafy.backend.game.domain.RoomInfo;
import com.ssafy.backend.game.dto.RoomDto;

public interface FriendlyService {

    RoomInfo createRoom(RoomDto roomDto);

}
