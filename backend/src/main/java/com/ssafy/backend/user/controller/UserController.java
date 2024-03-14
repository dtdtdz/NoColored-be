package com.ssafy.backend.user.controller;

import com.ssafy.backend.user.dto.UserSignDto;
import com.ssafy.backend.user.entity.UserProfile;
import com.ssafy.backend.user.service.UserService;
import com.ssafy.backend.user.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService, JwtUtil jwtUtil){
        this.userService = userService;

    }
    @GetMapping("/guest")
    public ResponseEntity<String> guestSignUp(){
        UserProfile userProfile = userService.guestSignUp();
        String jwtToken = userService.generateToken(userProfile);
        return ResponseEntity.ok(jwtToken);
    }

    @PostMapping("/signup")

    public ResponseEntity<String> signUp(@RequestBody UserSignDto user){
        UserProfile userProfile = userService.signUp(user.getId(), user.getPassword(), user.getNickname());
        String jwtToken = userService.generateToken(userProfile);
        return ResponseEntity.ok(jwtToken);
    }
    @PostMapping("/login")
    private ResponseEntity<String> login(@RequestBody UserSignDto user){
        return ResponseEntity.ok(userService.login(user.getId(),user.getPassword()));
    }


}
