package com.ssafy.backend.play.service;

import com.ssafy.backend.game.domain.RoomInfo;
import com.ssafy.backend.game.dto.RoomDto;

import java.util.List;

public interface FriendlyService {

    RoomInfo createRoom(RoomDto roomDto);

    List<RoomDto> getPaginatedRoomList(int offset);

}
