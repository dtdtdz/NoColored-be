package com.ssafy.backend.game.controller;

import com.ssafy.backend.game.domain.GameInfo;
import com.ssafy.backend.game.domain.GameRoomDto;
import com.ssafy.backend.game.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ingame")
public class InGameController {

    private final GameService gameService;
    public InGameController(GameService gameService){
        this.gameService = gameService;
    }

    @GetMapping("/ready")
    private ResponseEntity<?> ready(@RequestHeader("Authorization") String token){
        GameRoomDto gameRoomDto = gameService.ready(token);
        if (gameRoomDto==null){
            return ResponseEntity.internalServerError().body("Game containing the player not found");
        }

        return ResponseEntity.ok(gameRoomDto);
    }

    @GetMapping("/ready/dummy")
    private ResponseEntity<?> readyDummy(@RequestHeader("Authorization") String token){
        GameRoomDto gameRoomDto = gameService.ready(token);
        if (gameRoomDto==null){
            return ResponseEntity.internalServerError().body("Game containing the player not found");
        }

        return ResponseEntity.ok("{\"mapId\":1,\"floorList\":[[5,5,6],[15,5,5],[23,5,5],[7,9,8],[20,9,5],[3,13,6],[13,13,7],[24,13,6],[9,17,15]],\"skins\":[\"\",\"\"]}");
    }
}
