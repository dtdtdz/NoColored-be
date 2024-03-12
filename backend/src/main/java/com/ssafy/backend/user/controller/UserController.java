package com.ssafy.backend.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserController {

    @PostMapping("/signup")
    public ResponseEntity<String> signup(){
        return ResponseEntity.ok("1");
    }
    @PostMapping("/login")
    public ResponseEntity<String> login(){
        return ResponseEntity.ok("1");
    }

}
