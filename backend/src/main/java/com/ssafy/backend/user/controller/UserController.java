package com.ssafy.backend.user.controller;

import com.ssafy.backend.user.dto.UserLoginDto;
import com.ssafy.backend.user.entity.UserProfile;
import com.ssafy.backend.user.service.UserService;
import com.ssafy.backend.user.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {


    private final UserService userService;
    public UserController(UserService userService, JwtUtil jwtUtil){
        this.userService = userService;

    }
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserLoginDto user){
        String userCode = userService.signUp(user.getId(), user.getPassword(), user.getNickname());
        String jwtToken = userService.generateToken(userCode);
        return ResponseEntity.ok(jwtToken);
    }
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLoginDto user){
        return ResponseEntity.ok(user.getId()+" 랑 "+user.getPassword());
    }

}
