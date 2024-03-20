package com.ssafy.backend.game.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ready")
public class GameController {
    @GetMapping
    private ResponseEntity<String> ready(){
        return ResponseEntity.ok("확인");
    }
}
