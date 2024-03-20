package com.ssafy.backend.play.service;

import com.ssafy.backend.game.domain.RoomInfo;
import com.ssafy.backend.game.domain.UserAccessInfo;
import com.ssafy.backend.game.dto.FriendlyRoomDto;
import com.ssafy.backend.game.dto.RoomDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface FriendlyService {

    RoomInfo createRoom(RoomDto roomDto);

    List<FriendlyRoomDto> getPaginatedRoomList(int offset);

    ResponseEntity<?> enterRoom(int code, int password, UserAccessInfo userAccessInfo);

}
