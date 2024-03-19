package com.ssafy.backend.play.controller;

import com.ssafy.backend.assets.SendTextMessageWrapper;
import com.ssafy.backend.game.domain.MapInfo;
import com.ssafy.backend.game.dto.RoomDto;
import com.ssafy.backend.play.service.RankingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/play/ranking")
public class RankingController {
    private final RankingService rankingService;
    public RankingController(RankingService rankingService){
        this.rankingService = rankingService;
    }
    @GetMapping
    private ResponseEntity<?> addMatchingList(@RequestHeader("Authorization") String token){
        rankingService.addMatchingList(token);
        return ResponseEntity.ok("매칭 시작");
    }
    @DeleteMapping
    private ResponseEntity<?> delMatchingList(@RequestHeader("Authorization") String token){
        rankingService.delMatchingList(token);
        return ResponseEntity.ok("매칭 취소");
    }

    @GetMapping("/test")
    private ResponseEntity<String> test() throws Exception{
        return ResponseEntity.ok(SendTextMessageWrapper.wrapAndConvertToJson("yes"));
    }
}
