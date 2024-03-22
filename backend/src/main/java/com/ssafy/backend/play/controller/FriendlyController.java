package com.ssafy.backend.play.controller;



import com.ssafy.backend.websocket.domain.UserAccessInfo;
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


    UserProfileDto p0=new UserProfileDto("abcdefg1","방장",330,500,100,true,1,"gold","url0","url1");
    UserProfileDto p1=new UserProfileDto("abcdefg2","유저1",330,500,100,true,1,"gold","url2","url3");
    UserProfileDto p2=new UserProfileDto("abcdefg3","유저2",30,500,2,false,1,"gold","1","1");
    UserRoomDto player0= new UserRoomDto(0,p0);
    UserRoomDto player1= new UserRoomDto(1,p1);
    UserRoomDto player2= new UserRoomDto(2,p2);
    UserRoomDto player3= null;

    UserRoomDto[] players=new UserRoomDto[]{player0,player1,player2,player3};
    RoomDto dummyRoomDto=new RoomDto("방제목","1234",0,"4321",players,1);


    @PatchMapping("/test")
    private ResponseEntity<?> enterRoomTest(){

        return ResponseEntity.ok(dummyRoomDto);
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

}
