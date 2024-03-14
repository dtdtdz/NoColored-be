package com.ssafy.backend.play.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/play/Friendly")
public class FriendlyController {
    @GetMapping
    private ResponseEntity<?> getRoomList(){
        return ResponseEntity.ok("");
    }
    @PatchMapping
    private ResponseEntity<?> enterRoom(){
        return ResponseEntity.ok("");
    }
    @PostMapping
    private ResponseEntity<?> createRoom(){
        return ResponseEntity.ok("");
    }
    @DeleteMapping
    private ResponseEntity<?> quitRoom(){
        return ResponseEntity.ok("");
    }
    @GetMapping("/start")
    private ResponseEntity<?> startGame(){
        return ResponseEntity.ok("");
    }
    @GetMapping("/ready")
    private ResponseEntity<?> readyGame(){
        return ResponseEntity.ok("");
    }
}
