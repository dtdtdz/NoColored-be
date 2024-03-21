package com.ssafy.backend.play.service;

import com.ssafy.backend.websocket.domain.UserAccessInfo;
import org.springframework.http.ResponseEntity;

public interface FriendlyService {

//    RoomInfo createRoom(RoomDto roomDto);
    ResponseEntity<?> createRoom(String roomTitle, String roomPassword, int mapId, UserAccessInfo userAccessInfo);

//    List<FriendlyRoomDto> getPaginatedRoomList(int offset);
    ResponseEntity<?> getRoomList(int offset);

    ResponseEntity<?> enterRoom(String code, String password, UserAccessInfo userAccessInfo);

    ResponseEntity<?> readyRoom(UserAccessInfo userAccessInfo, String roomCode);


}
