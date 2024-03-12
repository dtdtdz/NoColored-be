package com.ssafy.backend.user.controller;

import com.ssafy.backend.user.dto.UserLoginDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserController {

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserLoginDto user){
        return ResponseEntity.ok(user.getId()+"  "+user.getPassword());
    }
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLoginDto user){
        return ResponseEntity.ok(user.getId()+"  "+user.getPassword());
    }

}
