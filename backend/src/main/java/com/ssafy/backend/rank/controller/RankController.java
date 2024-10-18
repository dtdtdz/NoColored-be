package com.ssafy.backend.rank.controller;


import com.ssafy.backend.rank.dto.RankDto;
import com.ssafy.backend.rank.service.RankService;
import com.ssafy.backend.user.util.JwtUtil;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 랭킹 시스템에 대한 컨트롤러
 * 자기 자신의 랭킹과 상위 100명의 랭킹 리스트를 조회
 * 게스트 유저는 랭킹 리스트에서 제외됨
 */
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
