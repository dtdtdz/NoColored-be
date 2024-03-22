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
    public ResponseEntity<?> guestConvert(@RequestHeader("Authorization") String token,
                                                       @RequestBody UserSignDto user){
        if (user.getId().length() < 6 || user.getId().length() > 20) return ResponseEntity.badRequest().body("ID does not meet the length requirements (6-20 characters).");
        if (!user.getId().matches("[a-zA-Z0-9]*")) return ResponseEntity.badRequest().body("ID must contain only letters and numbers.");
        if (user.getPassword().length() < 6 || user.getPassword().length() > 20) return ResponseEntity.badRequest().body("Password does not meet the length requirements (6-20 characters).");
        if (!user.confirm()) return ResponseEntity.badRequest().body("Passwords do not match.");

        return ResponseEntity.ok(userService.guestConvert(token, user));
    }


    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody UserSignDto user){
        if (user.getId().length() < 6 || user.getId().length() > 20) return ResponseEntity.badRequest().body("ID does not meet the length requirements (6-20 characters).");
        if (!user.getId().matches("[a-zA-Z0-9]*")) return ResponseEntity.badRequest().body("ID must contain only letters and numbers.");
        if (user.getPassword().length() < 6 || user.getPassword().length() > 20) return ResponseEntity.badRequest().body("Password does not meet the length requirements (6-20 characters).");
        if (!user.confirm()) return ResponseEntity.badRequest().body("Passwords do not match.");

        UserProfile userProfile = userService.signUp(user.getId(), user.getPassword(), user.getNickname());
        return ResponseEntity.ok(userService.generateUserInfoDtoWithToken(userProfile));
    }
    @PostMapping("/login")
    private ResponseEntity<?> login(@RequestBody UserSignDto user){
        try {
            return ResponseEntity.ok(userService.login(user.getId(),user.getPassword()));
        } catch (Exception e){
            return ResponseEntity.badRequest().body("Login failed");
        }
    }

    @PatchMapping("/password")
    private ResponseEntity<String> changePwd(@RequestHeader("Authorization") String token,
                                             @RequestBody Map<String, String> map){
        try {
            userService.updatePassword(token, map.get("newPassword"),map.get("prePassword"));
            return ResponseEntity.ok("Password updated successfully.");
        } catch (Exception e){
//            throw e;
            return ResponseEntity.internalServerError().body("Failed to delete user");
        }
    }

    @PatchMapping("/nickname")
    private ResponseEntity<String> changeNickname(@RequestHeader("Authorization") String token,
                                                  @RequestBody Map<String, String> map){
        try {
            userService.updateNickname(token, map.get("nickname"));
            return ResponseEntity.ok("Nickname updated successfully.");
        } catch (Exception e){
            return ResponseEntity.internalServerError().body("Failed to update nickname");
        }
    }

    @DeleteMapping
    private ResponseEntity<String> deleteUser(@RequestHeader("Authorization") String token,
                                                  @RequestBody Map<String, String> map){
        try {
            userService.deleteUser(token, map.get("password"));
            return ResponseEntity.ok("User deleted successfully.");
        } catch (Exception e){
            return ResponseEntity.internalServerError().body("Failed to delete user");
        }
    }

    @GetMapping("/{id}")
    private ResponseEntity<Boolean> existsUserId(@PathVariable("id") String id){
        return ResponseEntity.ok(userService.existsUserId(id));
    }

    @GetMapping("/info/{userCode}")
    private ResponseEntity<?> findUserInfo(@PathVariable("userCode") String userCode){
        UserProfileDto userProfileDto = userService.findUserInfo(userCode);
        if (userProfileDto==null) return ResponseEntity.badRequest().body("Can't find user");
        return ResponseEntity.ok(userProfileDto);
    }
}
