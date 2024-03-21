package com.ssafy.backend.play.service;

import com.ssafy.backend.assets.SynchronizedSend;
import com.ssafy.backend.game.domain.MapInfo;
import com.ssafy.backend.game.domain.RoomInfo;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
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
    public synchronized ResponseEntity<?> createRoom(String roomTitle, String roomPassword, int mapId, UserAccessInfo userAccessInfo){
        // 비밀번호 4글자 검사
        if(roomPassword.length()!=4){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("비밀번호가 잘못된 형식입니다.");
        }

        // roomCode 세팅
        RoomInfo roomInfo=new RoomInfo();
        synchronized (FriendlyServiceImpl.class) { // 동기화 블록으로 클래스 레벨 락 사용
            if (roomCode >= 9999) {
                roomCode = 1000;
            } else {
                roomCode++;
            }
            roomInfo.setRoomCodeInt(roomCode); // 할당
        }

        // roomdto 세팅
        RoomDto roomDto=new RoomDto();
        roomDto.setRoomTitle(roomTitle);
        roomDto.setRoomCodeString(String.valueOf(roomInfo.getRoomCodeInt()));
        roomDto.setMasterIndex(0);
        roomDto.setRoomPassword(roomPassword);

        // userRoomDtos 세팅
        UserRoomDto[] players = new UserRoomDto[4];
        // 방장 세팅
        players[0]=new UserRoomDto();
//        UserRoomDto player=new UserRoomDto();
//        player.setUserIndex(0);
//        player.setPlayer(new UserProfileDto(userAccessInfo.getUserProfile()));
//        player.setReady(false);
        players[0]= new UserRoomDto();
        players[0].setUserIndex(0);
        players[0].setPlayer(new UserProfileDto(userAccessInfo.getUserProfile()));
        players[0].setReady(false);
        // 1번부터 3번까지 세팅
        for(int i=1;i<4;i++){
            players[i]= new UserRoomDto();
            players[i].setUserIndex(i);
            players[i].setPlayer(null);
            players[i].setReady(false);
        }
        roomDto.setPlayers(players);
        roomDto.setMapId(1); // 이거 고쳐야할듯

        // roominfo 세팅
        roomInfo.setUserAccessInfos(new UserAccessInfo[] {userAccessInfo,null,null,null});
        roomInfo.setRoomDto(roomDto);
        roomInfo.setGameStart(false);
        roomInfo.setMapInfo(new MapInfo());
        roomInfoMap.put(roomInfo.getRoomCodeInt(),roomInfo);

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
            sortedRooms.sort(Comparator.comparingInt(RoomInfo::getRoomCodeInt));

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
                friendlyRoomDto.setRoomTitle(roomInfo.getRoomDto().getRoomTitle());
                friendlyRoomDto.setRoomCode(roomInfo.getRoomDto().getRoomCodeString());
                friendlyRoomDto.setMapId(roomInfo.getMapInfo().getMapId());

                // 유저 수 계산
                int userNumber = (int) Arrays.stream(roomInfo.getUserAccessInfos()).filter(Objects::nonNull).count();
                friendlyRoomDto.setUserNumber(userNumber);

                paginatedFriendlyRooms.add(friendlyRoomDto);
            }
        }
        return ResponseEntity.ok(paginatedFriendlyRooms);
    }

    @Override
    public synchronized ResponseEntity<?> enterRoom(String code, String password, UserAccessInfo userAccessInfo) {

        RoomInfo roomInfo = roomInfoMap.get(code);

        // 방이 존재하지 않음
        if (roomInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("방이 존재하지 않습니다.");
        }

        // 비밀번호 불일치
        if (!(roomInfo.getRoomDto().getRoomPassword().equals(password))) {
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
                RoomDto roomDto=roomInfo.getRoomDto();
                // 입장하는 사람 정보가 담긴 UserProfileDto 만들어서 userRoomDtos에 반영
                UserRoomDto[] players=roomDto.getPlayers();
                UserProfileDto player=new UserProfileDto(userAccessInfo.getUserProfile());
                players[i].setPlayer(player);
                // roomDto에 userRoomDtos 반영
                roomDto.setPlayers(players);

                // roomInfo에 roomDto반영
                roomInfo.setRoomDto(roomDto);

                // 입장했다고 세션 뿌리기
                for(int j=0;j<4;j++){
                    UserAccessInfo tempUserAccessInfo = roomInfo.getUserAccessInfos()[j];
                    // if(tempUserAccessInfo != null && userRoomDtos[j]!=null)
                    if(tempUserAccessInfo != null){
                        SynchronizedSend.textSend(tempUserAccessInfo.getSession(),"newUser",players[i]);
                    }
                }
                return ResponseEntity.ok(roomDto);
            }
        }
        // 풀방
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("방에 들어갈 공간이 없습니다.");
    }

    @Override
    public ResponseEntity<?> readyRoom(UserAccessInfo userAccessInfo, String roomCode){
        // roominfo 찾기
        RoomInfo roomInfo = roomInfoMap.get(roomCode);

        // 방이 존재하지 않음
        if (roomInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("방이 존재하지 않습니다.");
        }

        UserAccessInfo[] userAccessInfos=roomInfo.getUserAccessInfos();
        // 내 위치 찾기
        for(int i=0;i<4;i++){
            // 찾으면 해당하는 userRoomDto의 레디 상태 변경
            if(userAccessInfos[i]==userAccessInfo){
                RoomDto roomDto=roomInfo.getRoomDto();
                UserRoomDto[] players=roomDto.getPlayers();
                // 상태 변경
                if(players[i].isReady()){ players[i].setReady(false); }
                else{ players[i].setReady(true); }
                roomDto.setPlayers(players);

                // roominfo에 반영
                roomInfo.setRoomDto(roomDto);

                // 변경했다고 세션 뿌리기
                for(int j=0;j<4;j++){
                    UserAccessInfo tempUserAccessInfo=roomInfo.getUserAccessInfos()[j];
                    if(tempUserAccessInfo!=null){
                        SynchronizedSend.textSend(tempUserAccessInfo.getSession(),"readyChange",players[i]);
                    }
                }

                // 겜 시작 여기서 해야할듯?
                
                // 리턴
                return ResponseEntity.ok(roomDto);
            }
        }
        // 방안에 없는 유저임
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저가 방 안에 존재하지 않습니다");
    }


}
