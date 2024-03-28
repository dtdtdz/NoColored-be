package com.ssafy.backend.play.controller;

import com.ssafy.backend.play.domain.RoomInfo;
import com.ssafy.backend.play.service.FriendlyServiceImpl;
import com.ssafy.backend.play.service.RankingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
        try {
            rankingService.addMatchingList(token);
        } catch (Exception e){
            if (e.getMessage().startsWith("Position conflict")){
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            }
            e.printStackTrace();
            throw e;
        }

        return ResponseEntity.ok("Started matching.");
    }
    @DeleteMapping
    private ResponseEntity<?> delMatchingList(@RequestHeader("Authorization") String token){
        rankingService.delMatchingList(token);
        return ResponseEntity.ok("Canceled matching");
    }

}
