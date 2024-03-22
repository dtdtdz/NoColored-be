package com.ssafy.backend.play.service;

import com.ssafy.backend.assets.SynchronizedSend;
import com.ssafy.backend.game.domain.MapInfo;
import com.ssafy.backend.game.domain.RoomInfo;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import com.ssafy.backend.game.dto.FriendlyRoomDto;
import com.ssafy.backend.game.dto.RoomDto;
import com.ssafy.backend.game.dto.UserRoomDto;
import com.ssafy.backend.user.dto.UserProfileDto;
import com.ssafy.backend.user.entity.UserProfile;
import com.ssafy.backend.websocket.domain.SendTextMessageType;
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
        players[0]= new UserRoomDto(0, userAccessInfo.getUserProfileDto());

        // 1번부터 3번까지 세팅
        for(int i=1;i<4;i++){
            players[i]= new UserRoomDto(i, null);
        }
        roomDto.setPlayers(players);
        roomDto.setMapId(1); // 이거 고쳐야할듯

        // roominfo 세팅
        roomInfo.setUserAccessInfos(new UserAccessInfo[] {userAccessInfo,null,null,null});
        roomInfo.setRoomDto(roomDto);
        roomInfo.setGameStart(false);
        roomInfo.setMapInfo(new MapInfo()); // 고치기
        userAccessInfo.setRoomInfo(roomInfo);
        roomInfoMap.put(roomInfo.getRoomCodeInt(),roomInfo);
        // 리턴
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
    public synchronized ResponseEntity<?> enterRoom(int code, String password, UserAccessInfo userAccessInfo) {
        RoomInfo roomInfo = roomInfoMap.get(code);
        // 방이 존재하지 않음
        if (roomInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("방이 존재하지 않습니다.");
        }
        // 비밀번호 불일치
        if (!password.equals(roomInfo.getRoomDto().getRoomPassword())) {
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
                UserProfileDto player = userAccessInfo.getUserProfileDto();
                players[i]=new UserRoomDto(i, player);

                // roomDto에 userRoomDtos 반영
                roomDto.setPlayers(players);

                // roomInfo에 roomDto반영
                roomInfo.setRoomDto(roomDto);
                userAccessInfo.setRoomInfo(roomInfo);

                // 입장했다고 세션 뿌리기
                for(int j=0;j<4;j++){
                    UserAccessInfo tempUserAccessInfo = roomInfo.getUserAccessInfos()[j];
                    // if(tempUserAccessInfo != null && userRoomDtos[j]!=null)
                    if(tempUserAccessInfo != null){
                        SynchronizedSend.textSend(tempUserAccessInfo.getSession(),SendTextMessageType.NEW_USER.getValue(), players[i]);
                    }
                }
                return ResponseEntity.ok(roomDto);
            }
        }
        // 풀방
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("방에 들어갈 공간이 없습니다.");
    }

    @Override
    public synchronized ResponseEntity<?> readyRoom(UserAccessInfo userAccessInfo){

        RoomInfo roomInfo = userAccessInfo.getRoomInfo();
        // 방이 존재하지 않음
        if (roomInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("방이 존재하지 않습니다.");
        }

        UserAccessInfo[] userAccessInfos=roomInfo.getUserAccessInfos();
        // 내 위치 찾기
        for(int i=0;i<4;i++){
            // 찾으면 해당하는 userRoomDto의 레디 상태 변경
            if(userAccessInfos[i] != null && userAccessInfos[i].getUserProfile().getUserCode().equals(userAccessInfo.getUserProfile().getUserCode())){
                RoomDto roomDto=roomInfo.getRoomDto();
                UserRoomDto[] players=roomDto.getPlayers();
                // 방장이면
                if(i==roomDto.getMasterIndex()){
                    // 방에 있는 유저 수
                    int userNumber = (int) Arrays.stream(userAccessInfos).filter(Objects::nonNull).count();
                    // 레디한 사람의 수를 센다
                    int readyCount=0;
                    for(int j=0;j<4;j++){
                        if(j==i||players[j]==null){continue;}
                        if(players[j].isReady()){readyCount++;}
                    }
                    // 혼자라면
                    if(userNumber==1){
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("혼자서 게임을 플레이 할 수 없습니다.");
                    }else{
                        // 모든 사람이 레디 했으면
                        if(readyCount==userNumber-1){
                            // 상태 변경
                            players[i].setReady(true);
                            roomDto.setPlayers(players);
                            // roominfo에 반영
                            roomInfo.setRoomDto(roomDto);
                            roomInfo.setGameStart(true);

                            // 변경했다고 세션 뿌리기
                            for(int j=0;j<4;j++){
                                UserAccessInfo tempUserAccessInfo=roomInfo.getUserAccessInfos()[j];
                                if(tempUserAccessInfo!=null){
                                    SynchronizedSend.textSend(tempUserAccessInfo.getSession(), SendTextMessageType.GAME_START.getValue(),null);
                                }
                            }
                            // 리턴
                            return ResponseEntity.ok("게임 시작");
                        }
                    }
                }else{
                    // 방장 아님
                    // 상태 변경
                    if(players[i].isReady()){
                        players[i].setReady(false);
                        roomDto.setPlayers(players);

                        // roominfo에 반영
                        roomInfo.setRoomDto(roomDto);

                        // 변경했다고 세션 뿌리기
                        for(int j=0;j<4;j++){
                            UserAccessInfo tempUserAccessInfo=roomInfo.getUserAccessInfos()[j];
                            if(tempUserAccessInfo!=null){
                                SynchronizedSend.textSend(tempUserAccessInfo.getSession(),SendTextMessageType.READY_OFF.getValue(), i);
                            }
                        }
                        // 리턴
                        return ResponseEntity.ok("레디 해제");
                    }
                    else{
                        players[i].setReady(true);
                        roomDto.setPlayers(players);

                        // roominfo에 반영
                        roomInfo.setRoomDto(roomDto);

                        // 변경했다고 세션 뿌리기
                        for(int j=0;j<4;j++){
                            UserAccessInfo tempUserAccessInfo=roomInfo.getUserAccessInfos()[j];
                            if(tempUserAccessInfo!=null){
                                SynchronizedSend.textSend(tempUserAccessInfo.getSession(),SendTextMessageType.READY_ON.getValue(), i);
                            }
                        }
                        // 리턴
                        return ResponseEntity.ok("레디 성공");
                    }
                }
            }
        }
        // 방안에 없는 유저임
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저가 방 안에 존재하지 않습니다.");
    }

    @Override
    public ResponseEntity<?> renewRoom(UserAccessInfo userAccessInfo, String title, String password, int mapId){

        RoomInfo roomInfo = userAccessInfo.getRoomInfo();
        // 방이 존재하지 않음
        if (roomInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("방이 존재하지 않습니다.");
        }

        UserAccessInfo[] userAccessInfos=roomInfo.getUserAccessInfos();
        // 내 위치 찾기
        for(int i=0;i<4;i++) {
            // 찾으면 해당하는 userRoomDto의 상태 변경
            // userAccessInfos[i] == userAccessInfo
            if (userAccessInfos[i] != null && userAccessInfos[i].getUserProfile().getUserCode().equals(userAccessInfo.getUserProfile().getUserCode())) {
                RoomDto roomDto = roomInfo.getRoomDto();
                // 방장이면
                if (i == roomDto.getMasterIndex()) {
                    roomDto.setRoomTitle(title);
                    roomDto.setRoomPassword(password);
                    roomInfo.setRoomDto(roomDto);
                    roomInfo.setMapInfo(new MapInfo()); // 맵인포 고쳐야함!!!!!!!!!
                    return ResponseEntity.ok("대기실 정보 수정 완료입니다.");
                }
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("당신은 방장이 아닙니다.");
    }

    @Override
    public synchronized ResponseEntity<?> quitRoom(UserAccessInfo userAccessInfo){

        RoomInfo roomInfo = userAccessInfo.getRoomInfo();
        // 방이 존재하지 않음
        if (roomInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("방이 존재하지 않습니다.");
        }

        UserAccessInfo[] userAccessInfos=roomInfo.getUserAccessInfos();
        // 내 위치 찾기
        for(int i=0;i<4;i++) {
            // 찾으면 해당하는 userRoomDto의 상태 변경
            if (userAccessInfos[i] != null && userAccessInfos[i].getUserProfile().getUserCode().equals(userAccessInfo.getUserProfile().getUserCode())) {
                RoomDto roomDto = roomInfo.getRoomDto();
                UserRoomDto[] players=roomDto.getPlayers();
                // 방장이면
                if (i == roomDto.getMasterIndex()) {
                    // 방에 있는 유저 수
                    int userNumber = (int) Arrays.stream(userAccessInfos).filter(Objects::nonNull).count();

                    // 방장 혼자 있다면
                    if(userNumber==1){
                        // 자신 정보 바꾸고 맵에서 방 삭제
                        userAccessInfo.clearPosition();
                        roomInfoMap.remove(roomInfo.getRoomCodeInt());
                        return ResponseEntity.ok("방장이 방을 나가 대기실이 삭제됩니다.");
                    }else{
                        // 방장을 넘겨줄 사람 찾기
                        int startIndex=i+1;
                        // 최대 3번 탐색
                        for(int j=0;j<3;j++){
                            // 범위 벗어나면
                            if(startIndex>3){ startIndex-=4; }
                            // 넘겨줄 사람 찾으면 넘기기
                            if(userAccessInfos[startIndex]!=null){
                                roomDto.setMasterIndex(startIndex);
                                // 자신 정보 바꾸기
                                players[i] = null;
                                userAccessInfos[i]=null;
                                userAccessInfo.clearPosition();
                                roomInfo.setUserAccessInfos(userAccessInfos);
                                roomInfo.setRoomDto(roomDto);
                                // 변경했다고 세션 뿌리기
                                for(int k=0;k<4;k++){
                                    UserAccessInfo tempUserAccessInfo=roomInfo.getUserAccessInfos()[k];
                                    if(tempUserAccessInfo!=null){
                                        SynchronizedSend.textSend(tempUserAccessInfo.getSession(),SendTextMessageType.QUIT_MASTER.getValue(), i);
                                    }
                                }
                                return ResponseEntity.ok(roomDto);
                            }else{
                                startIndex++;
                            }
                        }
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("다른 플레이어를 찾을 수 없습니다.");
                    }
                }else{
                    // 방장 아니면
                    // 자신 위치의 정보 초기화
                    players[i] = null;
                    // roomInfo 반영
                    userAccessInfos[i]=null;
                    userAccessInfo.clearPosition();
                    roomInfo.setUserAccessInfos(userAccessInfos);

                    // 변경했다고 세션 뿌리기
                    for(int j=0;j<4;j++){
                        UserAccessInfo tempUserAccessInfo=roomInfo.getUserAccessInfos()[j];
                        if(tempUserAccessInfo!=null){
                            SynchronizedSend.textSend(tempUserAccessInfo.getSession(),SendTextMessageType.QUIT_PLAYER.getValue(), i);
                        }
                    }
                }
                return ResponseEntity.ok(roomDto);
            }
        }
        // 방에 플레이어가 없음
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("플레이어를 찾을 수 없습니다.");
    }

}
