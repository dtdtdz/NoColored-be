package com.ssafy.backend.play.service;

import com.ssafy.backend.play.domain.RoomInfo;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

public interface FriendlyService {

    ResponseEntity<?> createRoom(String roomTitle, String roomPassword, int mapId, UserAccessInfo userAccessInfo);

    ResponseEntity<?> getRoomList(int offset);

    ResponseEntity<?> findRoomId(int code, String password);

    ResponseEntity<?> enterRoom(UUID uuid, UserAccessInfo userAccessInfo);

    ResponseEntity<?> readyRoom(UserAccessInfo userAccessInfo);

    ResponseEntity<?> renewRoom(UserAccessInfo userAccessInfo, String title, String password, int mapId);

    ResponseEntity<?> quitRoomRequest(UserAccessInfo userAccessInfo);

    void sendRoomDto(RoomInfo roomInfo);
    public void quitRoom(UserAccessInfo userAccessInfo);

}
