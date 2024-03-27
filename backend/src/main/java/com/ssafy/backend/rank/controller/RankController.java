package com.ssafy.backend.rank.controller;


import com.ssafy.backend.rank.dto.RankDto;
import com.ssafy.backend.rank.dto.RankInfoDto;
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

    // 내 랭크 보기
    @GetMapping
    public ResponseEntity<RankDto> getRank(@RequestHeader("Authorization") String token){
        return ResponseEntity.ok(rankService.getRank(token));
    }

    // 상위 100명 랭크 가져오기
    @GetMapping("/list")
    public ResponseEntity<?> getRankList(){
        return ResponseEntity.ok(rankService.getRankList());
    }


}
