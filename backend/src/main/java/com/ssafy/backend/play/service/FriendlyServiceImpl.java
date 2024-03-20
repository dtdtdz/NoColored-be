package com.ssafy.backend.play.service;

import com.ssafy.backend.assets.SynchronizedSend;
import com.ssafy.backend.game.domain.RoomInfo;
import com.ssafy.backend.game.domain.UserAccessInfo;
import com.ssafy.backend.game.dto.FriendlyRoomDto;
import com.ssafy.backend.game.dto.RoomDto;
import com.ssafy.backend.user.dto.UserProfileDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class FriendlyServiceImpl implements FriendlyService {

    // 방 번호
    public static int roomCode=1000;

    private Map<Integer, RoomInfo> roomInfoMap =Collections.synchronizedMap(new HashMap<>());

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

        RoomInfo roomInfo = new RoomInfo();
//        roomInfo.setGameId(roomDto.getGameId());
        roomInfo.setTitle(roomDto.getTitle());
        roomInfo.setPassword(roomDto.getPassword());
        roomInfo.setCode(roomDto.getCode());
        roomInfo.setMaster(roomDto.getMaster());
        roomInfo.setReadyState(roomDto.getReadyState());
        roomInfo.setMapInfo(roomDto.getMapInfo());

        // 맵에 추가
        roomInfoMap.put(roomDto.getCode(), roomInfo);
        return roomInfo;
    }

    // 5페이지 분량 방 가져오기
    @Override
    public List<FriendlyRoomDto> getPaginatedRoomList(int offset) {
        final int roomsPerPage = 6;
        final int maxPages = 5;
        final int maxRooms = roomsPerPage * maxPages;

        List<FriendlyRoomDto> paginatedFriendlyRooms = new ArrayList<>();

        synchronized (roomInfoMap){
            // roomInfoMap에서 RoomInfo 객체들을 방 코드 순서로 정렬
            List<RoomInfo> sortedRooms = new ArrayList<>(roomInfoMap.values());
            sortedRooms.sort(Comparator.comparingInt(RoomInfo::getCode));

            // 페이징을 위한 계산
            int totalRooms = sortedRooms.size();
            int startIndex = (offset - 1) * maxRooms;
            if (startIndex >= totalRooms) {
                return Collections.emptyList(); // 시작 인덱스가 범위를 벗어나면 빈 리스트 반환
            }
            int endIndex = Math.min(startIndex + maxRooms, totalRooms);

            // 페이징된 목록 생성
            for (int i = startIndex; i < endIndex; i++) {
                RoomInfo roomInfo = sortedRooms.get(i);
                FriendlyRoomDto friendlyRoomDto = new FriendlyRoomDto();
                friendlyRoomDto.setRoomTitle(roomInfo.getTitle());
                friendlyRoomDto.setRoomCode(roomInfo.getCode());
                friendlyRoomDto.setMapId(roomInfo.getMapInfo().getMapId());

                // 유저 수 계산
                int userNumber = (int) Arrays.stream(roomInfo.getUserArr()).filter(Objects::nonNull).count();
                friendlyRoomDto.setUserNumber(userNumber);

                paginatedFriendlyRooms.add(friendlyRoomDto);
            }
        }
        return paginatedFriendlyRooms;
    }

    @Override
    public synchronized ResponseEntity<?> enterRoom(int code, int password, UserAccessInfo userAccessInfo) {

        RoomInfo roomInfo = roomInfoMap.get(code);

        // 방이 존재하지 않음
        if (roomInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("방이 존재하지 않습니다.");
        }

        // 비밀번호 불일치
        if (roomInfo.getPassword()!= password) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("틀린 비밀번호입니다.");
        }

        // 들어갈 곳 찾기
        for (int i = 0; i < 4; i++) {
            if (roomInfo.getUserArr()[i] == null) {
                roomInfo.getUserArr()[i] = userAccessInfo;

                // RoomInfo를 RoomDto로 변환
                RoomDto roomDto = new RoomDto();
                roomDto.setTitle(roomInfo.getTitle());
                roomDto.setPassword(roomInfo.getPassword());
                roomDto.setCode(roomInfo.getCode());
                roomDto.setMaster(roomInfo.getMaster());
                roomDto.setReadyState(roomInfo.getReadyState());

                // 세션에 뿌릴 map
                Map<Integer, UserProfileDto> dataMap = new HashMap<>();
                dataMap.put(i,new UserProfileDto(userAccessInfo.getUserProfile()));

                UserProfileDto[] userProfileDtos = new UserProfileDto[4];
                for (int j = 0; j < 4; j++) {
                    UserAccessInfo tempUserAccessInfo = roomInfo.getUserArr()[j];
                    if (tempUserAccessInfo != null) { // 널 체크 추가

                        // 방에 다른 사람들에게 세션 뿌려주기
                        SynchronizedSend.textSend(tempUserAccessInfo.getSession(),"newUser",dataMap);

                        userProfileDtos[j] = new UserProfileDto(tempUserAccessInfo.getUserProfile());
                    }
                }

                roomDto.setUserArr(userProfileDtos);
                roomDto.setMapInfo(roomInfo.getMapInfo());

                return ResponseEntity.ok(roomDto);
            }
        }
        // 풀방
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("방에 들어갈 공간이 없습니다.");
    }


}
