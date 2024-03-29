package com.ssafy.backend.game.controller;

import com.ssafy.backend.game.domain.GameRoomDto;
import com.ssafy.backend.game.domain.ResultInfo;
import com.ssafy.backend.game.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/ingame")
public class InGameController {

    private final GameService gameService;
    public InGameController(GameService gameService){
        this.gameService = gameService;
    }

    @GetMapping("/ready")
    private ResponseEntity<?> ready(@RequestHeader("Authorization") String token){
        System.out.println("ready");
        GameRoomDto gameRoomDto = gameService.ready(token);
        if (gameRoomDto==null){
            return ResponseEntity.internalServerError().body("Game containing the player not found");
        }

        return ResponseEntity.ok(gameRoomDto);
    }

    // 플레이어마다 다른 resultdto
    @GetMapping
    public ResponseEntity<?> getGameResultData(@RequestHeader("Authorization") String token){

        return ResponseEntity.ok(gameService.getResult(token));
    }

}
