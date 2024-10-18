package com.ssafy.backend.game.controller;

import com.ssafy.backend.game.domain.GameRoomDto;
import com.ssafy.backend.game.domain.ResultInfo;
import com.ssafy.backend.game.dto.ResultDto;
import com.ssafy.backend.game.dto.RewardDto;
import com.ssafy.backend.game.dto.TierDto;
import com.ssafy.backend.game.dto.UserResultDto;
import com.ssafy.backend.game.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;
/**
 * 유저가 게임으로 이동한 후 사용하는 컨트롤러
 * 유저 준비상태 확인, 게임결과 데이터 요청을 처리
 */
@RestController
@RequestMapping("/ingame")
public class InGameController {

    private final GameService gameService;
    public InGameController(GameService gameService){
        this.gameService = gameService;
    }
    /**
     * 유저가 게임씬으로 성공적으로 이동후 요청
     * 요청 결과로 유저가 필요한 게임 정보를 클라이언트로 보냄
     * 모든 유저의 준비가 확인되거나 대기시간이 지나면 게임을 시작
     */ 
    @GetMapping("/ready")
    private ResponseEntity<?> ready(@RequestHeader("Authorization") String token){
        System.out.println("ready");
        GameRoomDto gameRoomDto = gameService.ready(token);
        if (gameRoomDto==null){
            return ResponseEntity.internalServerError().body("Game containing the player not found");
        }

        return ResponseEntity.ok(gameRoomDto);
    }

    // 플레이어마다 다른 결과를 제공
    @GetMapping
    public ResponseEntity<?> getGameResultData(@RequestHeader("Authorization") String token){

        return ResponseEntity.ok(gameService.getResult(token));
    }

}
