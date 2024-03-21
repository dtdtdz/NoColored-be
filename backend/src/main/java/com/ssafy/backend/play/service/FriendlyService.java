package com.ssafy.backend.play.service;

import com.ssafy.backend.game.domain.RoomInfo;
import com.ssafy.backend.game.domain.UserAccessInfo;
import com.ssafy.backend.game.dto.FriendlyRoomDto;
import com.ssafy.backend.game.dto.RoomDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface FriendlyService {

//    RoomInfo createRoom(RoomDto roomDto);
    ResponseEntity<?> createRoom(String roomTitle, String roomPassword, int mapId, UserAccessInfo userAccessInfo);

//    List<FriendlyRoomDto> getPaginatedRoomList(int offset);
    ResponseEntity<?> getRoomList(int offset);

    ResponseEntity<?> enterRoom(String code, String password, UserAccessInfo userAccessInfo);

    ResponseEntity<?> readyRoom(UserAccessInfo userAccessInfo, String roomCode);


}
