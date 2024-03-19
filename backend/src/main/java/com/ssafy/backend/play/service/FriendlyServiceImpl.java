package com.ssafy.backend.play.service;

import com.ssafy.backend.game.domain.RoomInfo;
import com.ssafy.backend.game.dto.FriendlyRoomDto;
import com.ssafy.backend.game.dto.RoomDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Service
public class FriendlyServiceImpl implements FriendlyService {

    // 방 번호
    public static int roomCode=1000;

    public static List<RoomDto> roomDtoList= Collections.synchronizedList(new ArrayList<>());

    // mapinfo리스트로 가지고있다가 return시 dto로 바꿔주기
    public static List<RoomInfo> roomInfoList= Collections.synchronizedList(new ArrayList<>());

//    public static int roomCode=1000;
//
//    private synchronized int getNextCode() {
//        if (roomCode >= 9999) {
//            roomCode = 1000;
//        } else {
//            roomCode++;
//        }
//        return roomCode;
//    }

    // 대기실 생성
    @Override
    public synchronized RoomInfo createRoom(RoomDto roomDto) {

        // getNextCode 로직을 여기에 직접 포함시킴
        synchronized (FriendlyServiceImpl.class) { // 동기화 블록으로 클래스 레벨 락 사용
            if (roomCode >= 9999) {
                roomCode = 1000;
            } else {
                roomCode++;
            }
            roomDto.setCode(roomCode); // 할당
        }

        // 리스트에 추가
        // roomDtoList.add(roomDto);

        RoomInfo roomInfo = new RoomInfo();
//        roomInfo.setGameId(roomDto.getGameId());
        roomInfo.setTitle(roomDto.getTitle());
        roomInfo.setPassword(roomDto.getPassword());
        roomInfo.setCode(roomDto.getCode());
        roomInfo.setMaster(roomDto.getMaster());
        roomInfo.setMapInfo(roomDto.getMapInfo());

        // 리스트에 추가
        roomInfoList.add(roomInfo);
        return roomInfo;
    }

    // 5페이지 분량 방 가져오기
    @Override
    public List<FriendlyRoomDto> getPaginatedRoomList(int offset) {
        final int roomsPerPage = 6;
        final int maxPages = 5;
        final int maxRooms = roomsPerPage * maxPages;

        // 가져올 방 번호
        int startIndex = (offset - 1) * roomsPerPage;
        int endIndex;

        List<FriendlyRoomDto> paginatedFriendlyRooms = new ArrayList<>();

        synchronized (roomInfoList){
            // 시작 인덱스가 전체 리스트 크기를 넘어가는 경우 빈 리스트 반환
            if (startIndex >= roomInfoList.size()) {
                return Collections.emptyList();
            }

            endIndex = Math.min(startIndex + roomsPerPage, roomInfoList.size());

            for (int i = startIndex; i < endIndex; i++) {
                RoomInfo roomInfo = roomInfoList.get(i);
                FriendlyRoomDto friendlyRoomDto = new FriendlyRoomDto();
                friendlyRoomDto.setRoomTitle(roomInfo.getTitle()); // roomTitle 설정
                friendlyRoomDto.setRoomCode(roomInfo.getCode());   // roomCode 설정
                friendlyRoomDto.setMapId(roomInfo.getMapInfo().getMapId());         // mapId 설정
                friendlyRoomDto.setUserNumber(roomInfo.getUserArr().length);// userNumber 설정
                paginatedFriendlyRooms.add(friendlyRoomDto);
            }
        }

        return paginatedFriendlyRooms;
    }


}
