package com.ssafy.backend.play.controller;

import com.ssafy.backend.game.domain.GameInfo;
import com.ssafy.backend.game.domain.RoomInfo;
import com.ssafy.backend.game.domain.UserAccessInfo;
import com.ssafy.backend.game.dto.FriendlyRoomDto;
import com.ssafy.backend.game.dto.RoomDto;
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
    @GetMapping("/{offset}")
    private ResponseEntity<?> getRoomList(@PathVariable("offset") int offset){
        List<FriendlyRoomDto> roomInfoList=friendlyService.getPaginatedRoomList(offset);
        return ResponseEntity.ok(roomInfoList);
    }
    @PatchMapping
    private ResponseEntity<?> enterRoom(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> requestBody ){

        String code=(String) requestBody.get("roomCode");
        String password=(String) requestBody.get("roomPassword");


        return ResponseEntity.ok("");
    }
    @PostMapping()
    private ResponseEntity<?> createRoom(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> requestBody){

        String roomTitle = (String) requestBody.get("roomTitle");
        String roomPassword = (String) requestBody.get("roomPassword");
        int mapId = (int) requestBody.get("mapId");
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);

        RoomDto roomDto = new RoomDto(userAccessInfo, mapId, roomTitle, roomPassword);
        RoomInfo roomInfo = friendlyService.createRoom(roomDto);
        
        // useraccesssinfo 따로 처리
        roomInfo.setUserArr(new UserAccessInfo[]{userAccessInfo, null, null, null});
        return ResponseEntity.ok(roomDto);
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
