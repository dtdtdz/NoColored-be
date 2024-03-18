package com.ssafy.backend.play.service;

import com.ssafy.backend.game.domain.RoomInfo;
import com.ssafy.backend.game.dto.RoomDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Service
public class FriendlyServiceImpl implements FriendlyService {

    public static int roomCode=1000;

    public static List<RoomDto> roomDtoList= Collections.synchronizedList(new ArrayList<>());

    private synchronized int getNextCode() {
        if (roomCode >= 9999) {
            roomCode = 1000;
        } else {
            roomCode++;
        }
        return roomCode;
    }

    // 대기실 생성
    @Override
    public RoomInfo createRoom(RoomDto roomDto) {
        RoomInfo roomInfo = new RoomInfo();
//        roomInfo.setGameId(roomDto.getGameId());
        roomInfo.setTitle(roomDto.getTitle());
        roomInfo.setPassword(roomDto.getPassword());
        roomInfo.setCode(getNextCode());
        roomInfo.setMaster(roomDto.getMaster());
        roomInfo.setMapInfo(roomDto.getMapInfo());
        return roomInfo;
    }

    // 5페이지 분량 방 가져오기
    @Override
    public List<RoomDto> getPaginatedRoomList(int offset) {
        final int roomsPerPage = 6;
        final int maxPages = 5;
        final int maxRooms = roomsPerPage * maxPages;

        // 가져올 방 번호
        int startIndex = (offset - 1) * roomsPerPage;
        int endIndex;

        synchronized (roomDtoList) {

            // 시작 인덱스가 전체 리스트 크기를 넘어가는 경우 빈 리스트 반환
            if (startIndex >= roomDtoList.size()) {
                return Collections.emptyList();
            }

            endIndex = Math.min(startIndex + maxRooms, roomDtoList.size());

            return new ArrayList<>(roomDtoList.subList(startIndex, endIndex));
        }
    }


}
