package com.ssafy.backend.rank.controller;


import com.ssafy.backend.rank.dto.RankDto;
import com.ssafy.backend.rank.service.RankService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rank")
public class RankController {

    private final RankService rankService;
    public RankController(RankService rankService){
        this.rankService = rankService;
    }

    // dataNumber만큼 더미데이터 넣기
    @PostMapping("/{dataNumber}")
    public ResponseEntity<?> putRank(@PathVariable int dataNumber){
        return rankService.putRank(dataNumber);
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

    // redis 초기화
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearRedis(){
        return ResponseEntity.ok(rankService.clearRedis());
    }

}
