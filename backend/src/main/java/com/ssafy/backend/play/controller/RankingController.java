package com.ssafy.backend.play.controller;

import com.ssafy.backend.play.service.RankingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/play/ranking")
public class RankingController {
    private final RankingService rankingService;
    public RankingController(RankingService rankingService){
        this.rankingService = rankingService;
    }
    @PostMapping
    private ResponseEntity<?> addMatchingList(){
        return ResponseEntity.ok("");
    }

    @DeleteMapping
    private ResponseEntity<?> delMatchingList(){
        return ResponseEntity.ok("");
    }

}
