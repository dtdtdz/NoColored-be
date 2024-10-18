package com.ssafy.backend.play.controller;



import com.ssafy.backend.websocket.domain.UserAccessInfo;
import com.ssafy.backend.play.service.FriendlyService;
import com.ssafy.backend.user.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * 친선전 대기실을 위한 컨트롤러
 * 친선전 리스트를 페이징해서 조회
 * 친선전 대기실 생성, 입장, 퇴장, 변경
 * 대기실 유저의 준비 상태, 게임 시작 요청
 * 대기실은 갱신될 때마다 대기실 내 유저들에게 웹소켓으로 변경사항 전달
 */
@RestController
@RequestMapping("/play/friendly")
public class FriendlyController {
    private final FriendlyService friendlyService;
    private final JwtUtil jwtUtil;

    public FriendlyController(FriendlyService friendlyService, JwtUtil jwtUtil){
        this.friendlyService = friendlyService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("list/{offset}")
    private ResponseEntity<?> getRoomList(@RequestHeader("Authorization") String token, @PathVariable("offset") int offset){
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        if (userAccessInfo==null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        return friendlyService.getRoomList(offset);
    }

    @PostMapping("enter")
    private ResponseEntity<?> findRoomUuId(@RequestBody Map<String, Object> requestBody){
        int code = Integer.parseInt((requestBody.get("roomCode").toString()));
        String password=(String) requestBody.get("roomPassword");
        return friendlyService.findRoomId(code, password);
    }

    @GetMapping("lobby/{uuid}")
    private ResponseEntity<?> enterRoom(@RequestHeader("Authorization") String token, @PathVariable("uuid")UUID uuid){

        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);

        return friendlyService.enterRoom(uuid, userAccessInfo);
    }

    @PostMapping
    private ResponseEntity<?> createRoom(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> requestBody){
        String roomTitle = (String) requestBody.get("roomTitle");
        String roomPassword = (String) requestBody.get("roomPassword");
        int mapId = Integer.parseInt(requestBody.get("mapId").toString());
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        System.out.println(token);
        return friendlyService.createRoom(roomTitle,roomPassword,mapId,userAccessInfo);
    }

    @GetMapping("/ready")
    private ResponseEntity<?> readyRoom(@RequestHeader("Authorization") String token){
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        return friendlyService.readyRoom(userAccessInfo);
    }

    @PostMapping("/renew")
    private ResponseEntity<?> renewRoom(@RequestHeader("Authorization") String token,
                                        @RequestBody Map<String, Object> requestBody){
        String roomTitle = (String) requestBody.get("roomTitle");
        String roomPassword = (String) requestBody.get("roomPassword");
        int mapId = Integer.parseInt(requestBody.get("mapId").toString());
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        return friendlyService.renewRoom(userAccessInfo, roomTitle, roomPassword, mapId);
    }

    @GetMapping("/out")
    private ResponseEntity<?> quitRoom(@RequestHeader("Authorization") String token){
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        return friendlyService.quitRoomRequest(userAccessInfo);
    }

}
