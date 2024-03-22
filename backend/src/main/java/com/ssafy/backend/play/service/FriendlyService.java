package com.ssafy.backend.play.service;

import com.ssafy.backend.game.domain.RoomInfo;
import com.ssafy.backend.game.domain.UserAccessInfo;
import com.ssafy.backend.game.dto.FriendlyRoomDto;
import com.ssafy.backend.game.dto.RoomDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface FriendlyService {

    ResponseEntity<?> createRoom(String roomTitle, String roomPassword, int mapId, UserAccessInfo userAccessInfo);

    ResponseEntity<?> getRoomList(int offset);

    ResponseEntity<?> enterRoom(int code, String password, UserAccessInfo userAccessInfo);

    ResponseEntity<?> readyRoom(UserAccessInfo userAccessInfo);

    ResponseEntity<?> renewRoom(UserAccessInfo userAccessInfo, String title, String password, int mapId);

    ResponseEntity<?> quitRoom(UserAccessInfo userAccessInfo);

}
