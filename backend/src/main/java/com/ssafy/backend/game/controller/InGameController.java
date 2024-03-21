package com.ssafy.backend.game.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ingame")
public class InGameController {
    @GetMapping("/ready")
    private ResponseEntity<String> ready(@RequestHeader("Authorization") String token){
        return ResponseEntity.ok("확인");
    }
}
