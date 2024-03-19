package com.ssafy.backend.user.controller;

import com.ssafy.backend.user.dto.UserProfileDto;
import com.ssafy.backend.user.dto.UserSignDto;
import com.ssafy.backend.user.entity.UserProfile;
import com.ssafy.backend.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService){
        this.userService = userService;

    }
    @GetMapping("/guest")
    public ResponseEntity<UserProfileDto> guestSignUp(){
        UserProfile userProfile = userService.guestSignUp();
        return ResponseEntity.ok(userService.generateUserInfoDtoWithToken(userProfile));
    }
    @PostMapping("/guest")
    public ResponseEntity<UserProfileDto> guestConvert(@RequestHeader("Authorization") String token,
                                                       @RequestBody UserSignDto user){
        return ResponseEntity.ok(userService.guestConvert(token, user));
    }


    @PostMapping("/signup")
    public ResponseEntity<UserProfileDto> signUp(@RequestBody UserSignDto user){
        UserProfile userProfile = userService.signUp(user.getId(), user.getPassword(), user.getNickname());
        return ResponseEntity.ok(userService.generateUserInfoDtoWithToken(userProfile));
    }
    @PostMapping("/login")
    private ResponseEntity<UserProfileDto> login(@RequestBody UserSignDto user){
        return ResponseEntity.ok(userService.login(user.getId(),user.getPassword()));
    }

    @PatchMapping("/password")
    private ResponseEntity<String> changePwd(@RequestHeader("Authorization") String token,
                                             @RequestBody Map<String, String> map){
        try {
            userService.updatePassword(token, map.get("newPassword"),map.get("prePassword"));
            return ResponseEntity.ok("비밀번호 변경 성공");
        } catch (Exception e){
//            throw e;
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PatchMapping("/nickname")
    private ResponseEntity<String> changeNickname(@RequestHeader("Authorization") String token,
                                                  @RequestBody Map<String, String> map){
        try {
            userService.updateNickname(token, map.get("nickname"));
            return ResponseEntity.ok("닉네임 변경 성공");
        } catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping
    private ResponseEntity<String> deleteUser(@RequestHeader("Authorization") String token,
                                                  @RequestBody Map<String, String> map){
        try {
            userService.deleteUser(token, map.get("password"));
            return ResponseEntity.ok("유저 삭제 성공");
        } catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    private ResponseEntity<Boolean> existsUserId(@PathVariable("id") String id){
        return ResponseEntity.ok(userService.existsUserId(id));
    }

    @GetMapping("/info/{userCode}")
    private ResponseEntity<UserProfileDto> findUserInfo(@PathVariable("userCode") String userCode){
        UserProfileDto userProfileDto = userService.findUserInfo(userCode);
        if (userProfileDto==null) return ResponseEntity.badRequest().body(null);
        return ResponseEntity.ok(userProfileDto);
    }
}
