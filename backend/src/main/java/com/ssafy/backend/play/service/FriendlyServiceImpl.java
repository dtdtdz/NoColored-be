package com.ssafy.backend.play.service;

import com.ssafy.backend.assets.SynchronizedSend;
import com.ssafy.backend.game.domain.MapInfo;
import com.ssafy.backend.game.domain.RoomInfo;
import com.ssafy.backend.game.domain.UserAccessInfo;
import com.ssafy.backend.game.dto.FriendlyRoomDto;
import com.ssafy.backend.game.dto.RoomDto;
import com.ssafy.backend.game.dto.UserRoomDto;
import com.ssafy.backend.user.dto.UserProfileDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class FriendlyServiceImpl implements FriendlyService {

    // 방 번호
    public static int roomCode=1000;

    // roomcode, roominfo가 담긴 맵
    private Map<Integer, RoomInfo> roomInfoMap =Collections.synchronizedMap(new HashMap<>());

    // 대기실 생성
    @Override
    public synchronized ResponseEntity<?> createRoom(String roomTitle, int roomPassword, int mapId, UserAccessInfo userAccessInfo){

        // roomdto 세팅
        RoomDto roomDto=new RoomDto();
        synchronized (FriendlyServiceImpl.class) { // 동기화 블록으로 클래스 레벨 락 사용
            if (roomCode >= 9999) {
                roomCode = 1000;
            } else {
                roomCode++;
            }
            roomDto.setCode(roomCode); // 할당
        }
        roomDto.setTitle(roomTitle);
        roomDto.setMaster(0);

        // userRoomDtos 세팅
        UserRoomDto[] userRoomDtos = new UserRoomDto[4];
        // 방장 세팅
        userRoomDtos[0]= new UserRoomDto();
        userRoomDtos[0].setIndex(0);
        userRoomDtos[0].setUserProfileDto(new UserProfileDto(userAccessInfo.getUserProfile()));
        userRoomDtos[0].setReady(false);
        // 1번부터 3번까지 세팅
        for(int i=1;i<4;i++){
            userRoomDtos[i]= new UserRoomDto();
            userRoomDtos[i].setIndex(i);
            userRoomDtos[i].setUserProfileDto(null);
            userRoomDtos[i].setReady(false);
        }
        roomDto.setUserRoomDtos(userRoomDtos);
        roomDto.setMapInfo(new MapInfo());

        // roominfo 세팅
        RoomInfo roomInfo=new RoomInfo();
        roomInfo.setPassword(roomPassword);
        roomInfo.setUserAccessInfos(new UserAccessInfo[] {userAccessInfo,null,null,null});
        roomInfo.setRoomDto(roomDto);
        roomInfo.setGameStart(false);
        roomInfoMap.put(roomDto.getCode(),roomInfo);

        return ResponseEntity.ok(roomDto);
    }

    // 5페이지 분량 방 가져오기
    @Override
    public ResponseEntity<?> getRoomList(int offset){

        final int roomsPerPage = 6;
        final int maxPages = 5;
        final int maxRooms = roomsPerPage * maxPages;
        List<FriendlyRoomDto> paginatedFriendlyRooms = new ArrayList<>();

        synchronized (roomInfoMap){
            // roomInfoMap에서 RoomInfo 객체들을 방 코드 순서로 정렬
            List<RoomInfo> sortedRooms = new ArrayList<>(roomInfoMap.values());
            sortedRooms.sort(Comparator.comparingInt(roomInfo -> roomInfo.getRoomDto().getCode()));

            // 페이징을 위한 계산
            int totalRooms = sortedRooms.size();
            int startIndex = (offset - 1) * maxRooms;
            if (startIndex >= totalRooms) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            int endIndex = Math.min(startIndex + maxRooms, totalRooms);

            // 페이징된 목록 생성
            for (int i = startIndex; i < endIndex; i++) {
                RoomInfo roomInfo = sortedRooms.get(i);

                // 게임 시작한 방은 안가져온다
                if(roomInfo.isGameStart()){
                    endIndex++;
                    continue;
                }

                FriendlyRoomDto friendlyRoomDto = new FriendlyRoomDto();
                friendlyRoomDto.setTitle(roomInfo.getRoomDto().getTitle());
                friendlyRoomDto.setCode(roomInfo.getRoomDto().getCode());
                friendlyRoomDto.setMapId(roomInfo.getRoomDto().getMapInfo().getMapId());

                // 유저 수 계산
                int userNumber = (int) Arrays.stream(roomInfo.getUserAccessInfos()).filter(Objects::nonNull).count();
                friendlyRoomDto.setUserNumber(userNumber);

                paginatedFriendlyRooms.add(friendlyRoomDto);
            }
        }
        return ResponseEntity.ok(paginatedFriendlyRooms);
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

        // 겜 이미 시작했음
        if(roomInfo.isGameStart()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("이미 게임이 시작된 방입니다.");
        }

        // 들어갈 곳 찾기
        for (int i = 0; i < 4; i++) {

            // 들어갈 곳 찾음
            if (roomInfo.getUserAccessInfos()[i] == null) {
                roomInfo.getUserAccessInfos()[i] = userAccessInfo;

                // RoomInfo에서 정보 가져오기
                RoomDto roomDto = new RoomDto();
                roomDto.setTitle(roomInfo.getRoomDto().getTitle());
                roomDto.setCode(roomInfo.getRoomDto().getCode());
                roomDto.setMaster(roomInfo.getRoomDto().getMaster());
                // userRoomDto는 밑에도 써서 따로 빼서 처리
                UserRoomDto[] userRoomDtos=roomInfo.getRoomDto().getUserRoomDtos();
                // 입장하는 사람 정보가 담긴 UserProfileDto
                UserProfileDto userProfileDto=new UserProfileDto(userAccessInfo.getUserProfile());
                userRoomDtos[i].setUserProfileDto(userProfileDto);

                roomDto.setUserRoomDtos(userRoomDtos);

                // 입장했다고 세션 뿌리기
                for(int j=0;j<4;j++){
                    UserAccessInfo tempUserAccessInfo = roomInfo.getUserAccessInfos()[j];
                    if(tempUserAccessInfo != null && userRoomDtos[j]!=null){
                        SynchronizedSend.textSend(tempUserAccessInfo.getSession(),"newUser",userRoomDtos[i]);
                    }
                }

                roomDto.setMapInfo(roomInfo.getRoomDto().getMapInfo());

                return ResponseEntity.ok(roomDto);
            }
        }
        // 풀방
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("방에 들어갈 공간이 없습니다.");
    }

    @Override
    public synchronized ResponseEntity<?> enterRoomTest(int code, int password, UserAccessInfo userAccessInfo) {



        return ResponseEntity.ok("");
    }


}
