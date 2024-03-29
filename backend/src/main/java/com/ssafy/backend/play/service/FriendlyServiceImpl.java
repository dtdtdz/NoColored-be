package com.ssafy.backend.play.service;

import com.ssafy.backend.assets.SynchronizedSend;
import com.ssafy.backend.game.util.InGameCollection;
import com.ssafy.backend.play.domain.RoomInfo;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import com.ssafy.backend.play.dto.FriendlyRoomDto;
import com.ssafy.backend.play.dto.RoomDto;
import com.ssafy.backend.play.dto.UserRoomDto;
import com.ssafy.backend.user.dto.UserProfileDto;
import com.ssafy.backend.websocket.domain.SendTextMessageType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.util.*;


@Service
public class FriendlyServiceImpl implements FriendlyService {

    // 방 번호
    public static int roomCode=1000;

    // roomcode, roominfo가 담긴 맵
    private final Map<Integer, RoomInfo> roomInfoMap;
    private final Map<UUID, RoomInfo> uuidRoomInfoMap;

    private final InGameCollection inGameCollection;
    public FriendlyServiceImpl(InGameCollection inGameCollection){
        this.inGameCollection = inGameCollection;
        roomInfoMap = Collections.synchronizedMap(new LinkedHashMap<>()); // LinkedHashMap으로 바꿈
        uuidRoomInfoMap = Collections.synchronizedMap(new HashMap<>());;
    }

    // 대기실 생성
    @Override
    public synchronized ResponseEntity<?> createRoom(String roomTitle, String roomPassword, int mapId, UserAccessInfo userAccessInfo){
        // 비밀번호 숫자 4글자 검사
        if(!roomPassword.matches("^\\d{4}$")){
//            System.out.println(roomPassword);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid password format. (4 digits required)");
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
        roomDto.setRoomCode(String.valueOf(roomInfo.getRoomCodeInt()));
        roomDto.setMasterIndex(0);
        roomDto.setRoomPassword(roomPassword);
        roomDto.setRoomId(UUID.randomUUID());

        // userRoomDtos 세팅
        UserRoomDto[] players = new UserRoomDto[4];
        // 모든 유저 세팅
        for (int i=0; i<4; i++){
            players[i] = new UserRoomDto();
            players[i].setEmptyUser();
        }
        players[0].setUser(userAccessInfo.getUserProfileDto());

        roomDto.setPlayers(players);
        roomDto.setMapId(mapId); // 이거 고쳐야할듯

        // roominfo 세팅
        roomInfo.setUserAccessInfos(new UserAccessInfo[] {userAccessInfo,null,null,null});
        roomInfo.setRoomDto(roomDto);
        roomInfo.setGameStart(false);

        try {
            userAccessInfo.setRoomInfo(roomInfo);
        } catch (Exception e){
            if (userAccessInfo.getPosition() instanceof RoomInfo){
                ResponseEntity.status(HttpStatus.CONFLICT).body("Position conflict: "
                        +userAccessInfo.getRoomInfo().getClass()+":"
                        +userAccessInfo.getRoomInfo().getRoomDto().getRoomCode());
            } else {
                ResponseEntity.status(HttpStatus.CONFLICT).body("Position conflict: "
                        +userAccessInfo.getRoomInfo().getClass());
            }
        }
        roomInfoMap.put(roomInfo.getRoomCodeInt(),roomInfo);
        uuidRoomInfoMap.put(roomInfo.getRoomDto().getRoomId(), roomInfo);
        // 리턴
        return ResponseEntity.ok(roomInfo.getRoomDto().getRoomId());
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
            // LinkedHashMap 안되나? 만든 순서대로 정렬되는디
            List<RoomInfo> sortedRooms = new ArrayList<>(roomInfoMap.values());
//            sortedRooms.sort(Comparator.comparingInt(RoomInfo::getRoomCodeInt));
            Collections.reverse(sortedRooms);

            // 비었으면 하나만 만들어서 줌
            if(sortedRooms.isEmpty()){
                paginatedFriendlyRooms.add(new FriendlyRoomDto());
                return ResponseEntity.ok(paginatedFriendlyRooms);
            }

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
                friendlyRoomDto.setRoomCode(roomInfo.getRoomDto().getRoomCode());
                friendlyRoomDto.setMapId(roomInfo.getRoomDto().getMapId());
                // 유저 수 계산
                int userNumber = (int) Arrays.stream(roomInfo.getUserAccessInfos()).filter(Objects::nonNull).count();
                friendlyRoomDto.setUserNumber(userNumber);

                paginatedFriendlyRooms.add(friendlyRoomDto);
            }
        }
        return ResponseEntity.ok(paginatedFriendlyRooms);
    }

    @Override
    public ResponseEntity<?> findRoomId(int code, String password) {
        RoomInfo roomInfo = roomInfoMap.get(code);
        // 방이 존재하지 않음
        if (roomInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The room does not exist.");
        }
        // 비밀번호 불일치
        if (!password.equals(roomInfo.getRoomDto().getRoomPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect password.");
        }

        return ResponseEntity.ok(roomInfo.getRoomDto().getRoomId());
    }

    public ResponseEntity<?> enterRoom(UUID uuid, UserAccessInfo userAccessInfo) {
        RoomInfo roomInfo = uuidRoomInfoMap.get(uuid);
        // 방이 존재하지 않음
        if (roomInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The room does not exist.");
        }
        for (int i=0; i<4; i++){
            if (roomInfo.getUserAccessInfos()[i]==userAccessInfo)
                return ResponseEntity.ok(roomInfo.getRoomDto());
        }
        // 겜 이미 시작했음
        if(roomInfo.isGameStart()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("The game in this room has already started.");
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
                players[i].setUser(player);

                // roomDto에 userRoomDtos 반영
                roomDto.setPlayers(players);

                // roomInfo에 roomDto반영
                roomInfo.setRoomDto(roomDto);
                try {
                    userAccessInfo.setRoomInfo(roomInfo);
                } catch (Exception e){
                    if (userAccessInfo.getPosition() instanceof RoomInfo){
                        ResponseEntity.status(HttpStatus.CONFLICT).body("Position conflict: "
                                +userAccessInfo.getRoomInfo().getClass()+":"
                                +userAccessInfo.getRoomInfo().getRoomDto().getRoomCode());
                    } else {
                        ResponseEntity.status(HttpStatus.CONFLICT).body("Position conflict: "
                        +userAccessInfo.getRoomInfo().getClass());
                    }
                }


                // 입장했다고 세션 뿌리기
                for(int j=0;j<4;j++){
                    UserAccessInfo tempUserAccessInfo = roomInfo.getUserAccessInfos()[j];
                    // if(tempUserAccessInfo != null && userRoomDtos[j]!=null)
                    if(tempUserAccessInfo != null){
                        try {
                            SynchronizedSend.textSend(tempUserAccessInfo.getSession(),
                                    SendTextMessageType.ROOM_INFO.getValue(), roomDto);
                        } catch (Exception e){
                            System.out.println(e.getMessage());
                        }
                    }
                }
                return ResponseEntity.ok(roomDto);
            }
        }
        // 풀방
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("There is no space available in the room.");
    }

    @Override
    public synchronized ResponseEntity<?> readyRoom(UserAccessInfo userAccessInfo){

        RoomInfo roomInfo = userAccessInfo.getRoomInfo();
        // 방이 존재하지 않음
        if (roomInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The room does not exist.");
        }

        UserAccessInfo[] userAccessInfos=roomInfo.getUserAccessInfos();
        // 내 위치 찾기
        for(int i=0;i<4;i++){
            // 찾으면 해당하는 userRoomDto의 레디 상태 변경
            if(userAccessInfos[i] != null && userAccessInfos[i] == userAccessInfo){
                RoomDto roomDto=roomInfo.getRoomDto();
                UserRoomDto[] players=roomDto.getPlayers();
                // 방장이면
                if(i==roomDto.getMasterIndex()){
                    // 방에 있는 유저 수
                    int userNumber = (int) Arrays.stream(userAccessInfos).filter(Objects::nonNull).count();
                    // 레디한 사람의 수를 센다
                    int readyCount=0;
                    for(int j=0;j<4;j++){
                        if(j==i|| players[j].getUserCode().isEmpty()){continue;}
                        if(roomDto.getPlayers()[j].isReady()){readyCount++;}
                    }
                    // 혼자라면
                    if(userNumber==1){
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You cannot play the game alone.");
                    }else{
                        // 모든 사람이 레디 했으면
                        if(readyCount==userNumber-1){
                            // 상태 변경
                            roomDto.getPlayers()[i].setReady(true);

                            inGameCollection.addGame(roomInfo);
                            // roominfo에 반영
                            roomInfo.setGameStart(true);

                            // 변경했다고 세션 뿌리기
                            for(int j=0;j<4;j++){
                                UserAccessInfo tempUserAccessInfo=roomInfo.getUserAccessInfos()[j];
                                if(tempUserAccessInfo!=null){
                                    SynchronizedSend.textSend(tempUserAccessInfo.getSession(),
                                            SendTextMessageType.GAME_START.getValue(),null);
                                }
                            }
                            // 리턴
                            return ResponseEntity.ok("Game started.");
                        } else {
                            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Not all players are ready.");
                        }
                    }
                }else{
                    // 방장 아님
                    // 상태 변경
                    roomDto.getPlayers()[i].setReady(!roomDto.getPlayers()[i].isReady());

                    sendRoomDto(roomInfo);

                    return ResponseEntity.ok("Ready state changed: "+roomDto.getPlayers()[i].isReady());
                }
            }
        }
        // 방안에 없는 유저임
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist in the room.");
    }

    @Override
    public ResponseEntity<?> renewRoom(UserAccessInfo userAccessInfo, String title, String password, int mapId){

        RoomInfo roomInfo = userAccessInfo.getRoomInfo();
        // 방이 존재하지 않음
        if (roomInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The room does not exist.");
        }
        for (int i=0; i<4; i++){
            roomInfo.getRoomDto().getPlayers()[i].setReady(false);
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
                    sendRoomDto(roomInfo);
                    return ResponseEntity.ok("The room information has been updated.");
                }
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You are not the room owner.");
    }

    @Override
    public synchronized ResponseEntity<?> quitRoom(UserAccessInfo userAccessInfo){

        RoomInfo roomInfo = userAccessInfo.getRoomInfo();
        // 방이 존재하지 않음
        if (roomInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The room does not exist.");
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
                        return ResponseEntity.ok("The room owner has left, and the waiting room has been deleted.");
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
                                players[i].setEmptyUser();
                                userAccessInfos[i]=null;
                                userAccessInfo.clearPosition();
                                // 변경했다고 세션 뿌리기
                                for(int k=0;k<4;k++){
                                    UserAccessInfo tempUserAccessInfo=roomInfo.getUserAccessInfos()[k];
                                    if(tempUserAccessInfo!=null){
                                        SynchronizedSend.textSend(tempUserAccessInfo.getSession(),
                                                SendTextMessageType.ROOM_INFO.getValue(), roomDto);
                                    }
                                }
                                return ResponseEntity.ok(roomDto);
                            }else{
                                startIndex++;
                            }
                        }
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Unable to find other players.");
                    }
                }else{
                    // 방장 아니면
                    // 자신 위치의 정보 초기화
                    players[i].setEmptyUser();
                    // roomInfo 반영
                    userAccessInfos[i]=null;
                    userAccessInfo.clearPosition();

                    // 변경했다고 세션 뿌리기
                    for(int j=0;j<4;j++){
                        UserAccessInfo tempUserAccessInfo=roomInfo.getUserAccessInfos()[j];
                        if(tempUserAccessInfo!=null){
                            SynchronizedSend.textSend(tempUserAccessInfo.getSession(),
                                    SendTextMessageType.ROOM_INFO.getValue(), roomDto);
                        }
                    }
                }
                return ResponseEntity.ok(roomDto);
            }
        }
        // 방에 플레이어가 없음
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unable to find the player.");
    }
    private void sendRoomDto(RoomInfo roomInfo){
        for (UserAccessInfo userAccessInfo:roomInfo.getUserAccessInfos()){
            if (userAccessInfo!=null && userAccessInfo.getSession().isOpen()) {
                SynchronizedSend.textSend(userAccessInfo.getSession(),SendTextMessageType.ROOM_INFO.getValue(),roomInfo.getRoomDto() );
            }
        }
    }
}
