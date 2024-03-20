package com.ssafy.backend.play.service;

import com.ssafy.backend.game.domain.RoomInfo;
import com.ssafy.backend.game.domain.UserAccessInfo;
import com.ssafy.backend.game.dto.FriendlyRoomDto;
import com.ssafy.backend.game.dto.RoomDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface FriendlyService {

//    RoomInfo createRoom(RoomDto roomDto);
    ResponseEntity<?> createRoom(String roomTitle, int roomPassword, int mapId, UserAccessInfo userAccessInfo);

//    List<FriendlyRoomDto> getPaginatedRoomList(int offset);
    ResponseEntity<?> getRoomList(int offset);

    ResponseEntity<?> enterRoom(int code, int password, UserAccessInfo userAccessInfo);

    ResponseEntity<?> enterRoomTest(int code, int password, UserAccessInfo userAccessInfo);

}
