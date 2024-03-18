package com.ssafy.backend.user.controller;

import com.ssafy.backend.user.dto.UserInfoDto;
import com.ssafy.backend.user.dto.UserSignDto;
import com.ssafy.backend.user.entity.UserProfile;
import com.ssafy.backend.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService){
        this.userService = userService;

    }
    @GetMapping("/guest")
    public ResponseEntity<UserInfoDto> guestSignUp(){
        UserProfile userProfile = userService.guestSignUp();
        return ResponseEntity.ok(userService.generateUserInfoDtoWithToken(userProfile));
    }
    @PostMapping("/guest")
    public ResponseEntity<UserInfoDto> guestConvert(@RequestHeader("Authorization") String token,
                                                    @RequestBody UserSignDto user){
        return ResponseEntity.ok(userService.guestConvert(token, user));
    }


    @PostMapping("/signup")
    public ResponseEntity<UserInfoDto> signUp(@RequestBody UserSignDto user){
        UserProfile userProfile = userService.signUp(user.getId(), user.getPassword(), user.getNickname());
        return ResponseEntity.ok(userService.generateUserInfoDtoWithToken(userProfile));
    }
    @PostMapping("/login")
    private ResponseEntity<UserInfoDto> login(@RequestBody UserSignDto user){
        return ResponseEntity.ok(userService.login(user.getId(),user.getPassword()));
    }


}
