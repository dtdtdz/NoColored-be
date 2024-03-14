package com.ssafy.backend.play.controller;

import com.ssafy.backend.play.service.FriendlyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/play/Friendly")
public class FriendlyController {
    private final FriendlyService friendlyService;
    public FriendlyController(FriendlyService friendlyService){
        this.friendlyService = friendlyService;
    }
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
