package com.ssafy.backend.play.service;

import com.ssafy.backend.game.domain.GameInfo;
import com.ssafy.backend.game.domain.MapInfo;
import com.ssafy.backend.game.domain.RoomInfo;
import com.ssafy.backend.game.domain.UserAccessInfo;
import com.ssafy.backend.game.dto.RoomDto;
import com.ssafy.backend.user.dto.UserInfoDto;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;


@Service
public class FriendlyServiceImpl implements FriendlyService {

    public static int roomCode=1000;

    private synchronized int getNextCode() {
        if (roomCode >= 9999) {
            roomCode = 1000;
        } else {
            roomCode++;
        }
        return roomCode;
    }

    @Override
    public RoomInfo createRoom(RoomDto roomDto) {
        RoomInfo roomInfo = new RoomInfo();
        roomInfo.setGameId(roomDto.getGameId());
        roomInfo.setTitle(roomDto.getTitle());
        roomInfo.setPassword(roomDto.getPassword());
        roomInfo.setCode(roomCode);
        roomInfo.setMaster(roomDto.getMaster());
        roomInfo.setMapInfo(roomDto.getMapInfo());
        return roomInfo;
    }

}
