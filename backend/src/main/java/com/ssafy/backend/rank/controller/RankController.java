package com.ssafy.backend.rank.controller;


import com.ssafy.backend.rank.dto.RankDto;
import com.ssafy.backend.rank.service.RankService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rank")
public class RankController {

    private final RankService rankService;
    public RankController(RankService rankService){
        this.rankService = rankService;
    }
    @GetMapping
    public ResponseEntity<RankDto> getRank(@RequestHeader("Authorization") String token){

        return ResponseEntity.ok(rankService.getRank(token));
    }

//    @GetMapping("/rank")
//    public ResponseEntity<String>

}
