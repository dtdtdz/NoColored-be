package com.ssafy.backend.play.controller;



import com.ssafy.backend.game.domain.MapInfo;
import com.ssafy.backend.game.domain.RoomInfo;
import com.ssafy.backend.game.domain.UserAccessInfo;
import com.ssafy.backend.game.dto.FriendlyRoomDto;
import com.ssafy.backend.game.dto.RoomDto;
import com.ssafy.backend.game.dto.UserRoomDto;
import com.ssafy.backend.play.service.FriendlyService;
import com.ssafy.backend.user.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/play/friendly")
public class FriendlyController {
    private final FriendlyService friendlyService;
    private final JwtUtil jwtUtil;

    public FriendlyController(FriendlyService friendlyService, JwtUtil jwtUtil){
        this.friendlyService = friendlyService;
        this.jwtUtil = jwtUtil;
    }

    RoomDto dummyRoomDto=new RoomDto("dummy",1111,0,new UserRoomDto[]{null,null,null,null},null);

    RoomInfo dummyRoomInfo = new RoomInfo(1111,new UserAccessInfo[]{null,null,null,null},dummyRoomDto,false);

    @PatchMapping("/test")
    private ResponseEntity<?> enterRoomTest(){

        return ResponseEntity.ok(dummyRoomDto);
    }

    @GetMapping("/{offset}")
    private ResponseEntity<?> getRoomList(@PathVariable("offset") int offset){

//        List<FriendlyRoomDto> roomInfoList=friendlyService.getPaginatedRoomList(offset);
//        return ResponseEntity.ok(roomInfoList);

        return friendlyService.getRoomList(offset);
    }
    @PatchMapping
    private ResponseEntity<?> enterRoom(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> requestBody ){

        int code=(Integer) requestBody.get("roomCode");
        int password=Integer.parseInt(requestBody.get("roomPassword").toString());
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);

        return friendlyService.enterRoom(code, password, userAccessInfo);
    }

    @PostMapping()
    private ResponseEntity<?> createRoom(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> requestBody){

        // requestbody를 service로 보내서 한번에 처리 가능한거 아님??? 나중에 고쳐라
        String roomTitle = (String) requestBody.get("roomTitle");
        int roomPassword = Integer.parseInt(requestBody.get("roomPassword").toString());
        int mapId = Integer.parseInt(requestBody.get("mapId").toString());
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);

        return friendlyService.createRoom(roomTitle,roomPassword,mapId,userAccessInfo);
    }

    @DeleteMapping
    private ResponseEntity<?> quitRoom(){
        return ResponseEntity.ok("");
    }
    @GetMapping("/start")
    private ResponseEntity<?> startGame(){
        return ResponseEntity.ok("");
    }
    @GetMapping("/ready")
    private ResponseEntity<?> readyGame(){
        return ResponseEntity.ok("");
    }
}
