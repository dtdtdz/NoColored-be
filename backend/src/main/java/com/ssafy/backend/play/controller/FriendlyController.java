package com.ssafy.backend.play.controller;



import com.ssafy.backend.game.domain.MapInfo;
import com.ssafy.backend.game.domain.RoomInfo;
import com.ssafy.backend.game.domain.UserAccessInfo;
import com.ssafy.backend.game.dto.FriendlyRoomDto;
import com.ssafy.backend.game.dto.RoomDto;
import com.ssafy.backend.game.dto.UserRoomDto;
import com.ssafy.backend.play.service.FriendlyService;
import com.ssafy.backend.user.dto.UserProfileDto;
import com.ssafy.backend.user.entity.UserProfile;
import com.ssafy.backend.user.util.JwtUtil;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/play/friendly")
public class FriendlyController {
    private final FriendlyService friendlyService;
    private final JwtUtil jwtUtil;

    public FriendlyController(FriendlyService friendlyService, JwtUtil jwtUtil){
        this.friendlyService = friendlyService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/{offset}")
    private ResponseEntity<?> getRoomList(@PathVariable("offset") int offset){
        return friendlyService.getRoomList(offset);
    }
    @PatchMapping
    private ResponseEntity<?> enterRoom(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> requestBody ){

        int code=Integer.parseInt(requestBody.get("roomCode").toString());
        String password=(String) requestBody.get("roomPassword");
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);

        return friendlyService.enterRoom(code, password, userAccessInfo);
    }

    @PostMapping()
    private ResponseEntity<?> createRoom(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> requestBody){
        // requestbody를 service로 보내서 한번에 처리 가능한거 아님??? 나중에 고쳐라
        String roomTitle = (String) requestBody.get("roomTitle");
        String roomPassword = (String) requestBody.get("roomPassword");
        int mapId = Integer.parseInt(requestBody.get("mapId").toString());
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);

        return friendlyService.createRoom(roomTitle,roomPassword,mapId,userAccessInfo);
    }

    @PatchMapping("/ready")
    private ResponseEntity<?> readyRoom(@RequestHeader("Authorization") String token){
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        return friendlyService.readyRoom(userAccessInfo);
    }

    @PostMapping("/renew")
    private ResponseEntity<?> renewRoom(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> requestBody){
        String roomTitle=(String) requestBody.get("roomTitle");
        String roomPassword = (String) requestBody.get("roomPassword");
        int mapId = Integer.parseInt(requestBody.get("mapId").toString());
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        return friendlyService.renewRoom(userAccessInfo, roomTitle, roomPassword, mapId);
    }

    @PatchMapping("/out")
    private ResponseEntity<?> quitRoom(@RequestHeader("Authorization") String token){
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        return friendlyService.quitRoom(userAccessInfo);
    }







    // 테스트 코드들 모음
    // 테스트 코드들 모음
    // 테스트 코드들 모음
    // 테스트 코드들 모음
    // 테스트 코드들 모음
    // 테스트 코드들 모음

    UserProfileDto p0=new UserProfileDto(null,"abcdefg1","방장",330,100,true,1,"gold","url0","url1");
    UserProfileDto p1=new UserProfileDto(null,"abcdefg2","유저1",330,100,true,1,"gold","url2","url3");
    UserProfileDto p2=new UserProfileDto(null,"abcdefg3","유저2",30,2,false,1,"gold","url4","url5");
    UserProfileDto p3=new UserProfileDto(null,"abcdefg3","유저3",3000,1,false,1,"gold","url6","url7");
    UserRoomDto player0= new UserRoomDto(0,p0,false);
    UserRoomDto player1= new UserRoomDto(1,p1,true);
    UserRoomDto player2= new UserRoomDto(2,p2,false);
    UserRoomDto player3= new UserRoomDto(3,null,false);
    UserProfile userProfileUser1=new UserProfile(UUID.randomUUID(),"uscode01","uspro01",false,100L,"skinurl1","titleurl1",1000);
    UserProfile userProfileGuest1=new UserProfile(UUID.randomUUID(),"uscode02","uspro02",true,1000L,"skinurl2","titleurl2",1500);
    UserProfile userProfileUser2=new UserProfile(UUID.randomUUID(),"uscode03","uspro03",false,10L,"skinurl3","titleurl3",1234);
    UserProfile userProfileGuest2=new UserProfile(UUID.randomUUID(),"uscode04","uspro04",true,88L,"skinurl4","titleurl4",5555);
    UserAccessInfo userAccessInfoUser1=new UserAccessInfo(userProfileUser1);
    UserAccessInfo userAccessInfoGuest1=new UserAccessInfo(userProfileGuest1);
    UserAccessInfo userAccessInfoUser2=new UserAccessInfo(userProfileUser2);
    UserAccessInfo userAccessInfoGuest2=new UserAccessInfo(userProfileGuest2);
    UserAccessInfo[] userAccessInfoArr1=new UserAccessInfo[]{userAccessInfoUser1,userAccessInfoGuest1,userAccessInfoUser2,userAccessInfoGuest2};
    UserRoomDto[] players=new UserRoomDto[]{player0,player1,player2,player3};
    RoomDto roomDto1=new RoomDto("방제목","1001",0,"4321",players,1);
    MapInfo mapInfo1=new MapInfo();
    RoomInfo roomInfo1=new RoomInfo(userAccessInfoArr1,1001,roomDto1,false,mapInfo1);

    // 대기실 생성 테스트->확인 완료
    @PostMapping("/test/create")
    private ResponseEntity<?> createRoomTest(@RequestBody Map<String, Object> requestBody){
        String roomTitle = (String) requestBody.get("roomTitle");
        String roomPassword = (String) requestBody.get("roomPassword");
        int mapId = Integer.parseInt(requestBody.get("mapId").toString());
        UserAccessInfo userAccessInfo = userAccessInfoGuest1;
        // UserAccessInfo userAccessInfo = userAccessInfoUser1;
        return friendlyService.createRoomTest(roomTitle,roomPassword,mapId,userAccessInfo);
    }

    // 대기실 가져오기 테스트->확인 완료
    @GetMapping("/test/get/{offset}")
    private ResponseEntity<?> getRoomListTest(@PathVariable("offset") int offset){
        return friendlyService.getRoomListTest(offset);
    }

    // 대기실 입장 테스트->확인중
    @PatchMapping("/test/enter")
    private ResponseEntity<?> enterRoomTest( @RequestBody Map<String, Object> requestBody){
        int code=Integer.parseInt(requestBody.get("roomCode").toString());
        String password=(String) requestBody.get("roomPassword");
        UserAccessInfo userAccessInfo = userAccessInfoUser1;
        return friendlyService.enterRoomTest(code, password, userAccessInfo);
    }


    
}
