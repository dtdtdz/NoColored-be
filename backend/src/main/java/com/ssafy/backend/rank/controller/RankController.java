package com.ssafy.backend.rank.controller;


import com.ssafy.backend.rank.dto.RankDto;
import com.ssafy.backend.rank.service.RankService;
import com.ssafy.backend.user.util.JwtUtil;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rank")
public class RankController {

    private final RankService rankService;
    private final JwtUtil jwtUtil;
    public RankController(RankService rankService, JwtUtil jwtUtil){
        this.rankService = rankService;
        this.jwtUtil = jwtUtil;
    }

    // dataNumber만큼 더미데이터 넣기
    @PostMapping("/{dataNumber}")
    public ResponseEntity<?> putRank(@PathVariable int dataNumber){
        return rankService.putRank(dataNumber);
    }

    // 내 랭크 보기
    @GetMapping
    public ResponseEntity<?> getRank(@RequestHeader("Authorization") String token){
        UserAccessInfo user = jwtUtil.getUserAccessInfoRedis(token);
        return ResponseEntity.ok(rankService.getRank(user));
    }

    // 최대 상위 100명 랭크 가져오기
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
