package com.ssafy.backend.play.controller;

import com.ssafy.backend.play.service.FriendlyServiceImpl;
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
        return ResponseEntity.ok("Started Matching.");
    }
    @DeleteMapping
    private ResponseEntity<?> delMatchingList(@RequestHeader("Authorization") String token){
        rankingService.delMatchingList(token);
        return ResponseEntity.ok("Canceled Matching");
    }

}
