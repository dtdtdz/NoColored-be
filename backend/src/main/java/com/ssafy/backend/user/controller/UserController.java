package com.ssafy.backend.user.controller;

import com.ssafy.backend.user.dto.UserProfileDto;
import com.ssafy.backend.user.dto.UserSignDto;
import com.ssafy.backend.user.entity.UserProfile;
import com.ssafy.backend.user.service.UserService;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/token")
    public ResponseEntity<?> isTokenValid(@RequestHeader("Authorization") String token){
        return ResponseEntity.ok(userService.isTokenValid(token));
    }

    @GetMapping
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String token){
        UserProfileDto userProfileDto = userService.getUserProfileDto(token);
        if (userProfileDto==null){
            return ResponseEntity.badRequest().body("Token is invalid");
        } else {
            System.out.println(userProfileDto.isGuest());
            return ResponseEntity.ok(userProfileDto);
        }
    }

    @GetMapping("/guest")
    public ResponseEntity<String> guestSignUp(){
        UserAccessInfo userAccessInfo = userService.guestSignUp();
        return ResponseEntity.ok(userService.generateToken(userAccessInfo));
    }
    @PostMapping("/guest")
    public ResponseEntity<String> guestConvert(@RequestHeader("Authorization") String token,
                                                       @RequestBody UserSignDto user){
        if (user.getId().length() < 6 || user.getId().length() > 20) return ResponseEntity.badRequest().body("ID does not meet the length requirements (6-20 characters).");
        if (!user.getId().matches("[a-zA-Z0-9]*")) return ResponseEntity.badRequest().body("ID must contain only letters and numbers.");
        if (user.getPassword().length() < 6 || user.getPassword().length() > 20) return ResponseEntity.badRequest().body("Password does not meet the length requirements (6-20 characters).");
        if (!user.confirm()) return ResponseEntity.badRequest().body("Passwords do not match.");
        if (user.getNickname().length() < 2 || user.getNickname().length() > 9) return ResponseEntity.badRequest().body("Nickname does not meet the length requirements (6-20 characters).");
        try {
            return ResponseEntity.ok(userService.guestConvert(token, user));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Convert guest failed");
        }
    }


    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody UserSignDto user){
        if (user.getId().length() < 6 || user.getId().length() > 20) return ResponseEntity.badRequest().body("ID does not meet the length requirements (6-20 characters).");
        if (!user.getId().matches("[a-zA-Z0-9]*")) return ResponseEntity.badRequest().body("ID must contain only letters and numbers.");
        if (user.getPassword().length() < 6 || user.getPassword().length() > 20) return ResponseEntity.badRequest().body("Password does not meet the length requirements (6-20 characters).");
        if (!user.confirm()) return ResponseEntity.badRequest().body("Passwords do not match.");
        if (user.getNickname().length() < 2 || user.getNickname().length() > 9) return ResponseEntity.badRequest().body("Nickname does not meet the length requirements (6-20 characters).");

        UserAccessInfo userAccessInfo = userService.signUp(user.getId(), user.getPassword(), user.getNickname());
        try {
            return ResponseEntity.ok(userService.generateToken(userAccessInfo));
        } catch (Exception e){
            return ResponseEntity.internalServerError().body("User registration failed");
        }
    }
    @PostMapping("/login")
    private ResponseEntity<String> login(@RequestBody UserSignDto user){
        try {
            String token = userService.login(user.getId(),user.getPassword());
            if (token==null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("잘못된 접근입니다.");
            return ResponseEntity.ok(token);
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

    @PostMapping("/confirm")
    private ResponseEntity<Boolean> confirm(@RequestHeader("Authorization") String token,
                                            @RequestBody Map<String, String> map){

        return ResponseEntity.ok(userService.confirmUser(token, map.get("password")));
    }

    @DeleteMapping
    private ResponseEntity<String> deleteUser(@RequestHeader("Authorization") String token){
        try {
            userService.deleteUser(token);
            return ResponseEntity.ok("User deleted successfully.");
        } catch (Exception e){
            return ResponseEntity.internalServerError().body("Failed to delete user");
        }
    }

    @GetMapping("/dup/{id}")
    private ResponseEntity<Boolean> existsUserId(@PathVariable("id") String id){
        return ResponseEntity.ok(userService.existsUserId(id));
    }


    @GetMapping("/info/{userCode}")
    private ResponseEntity<?> findUserInfo(@PathVariable("userCode") String userCode){
        UserProfileDto userProfileDto = userService.findUserInfo(userCode);
        if (userProfileDto==null) return ResponseEntity.badRequest().body("Can't find user");

        return ResponseEntity.ok(userProfileDto);
    }

    @GetMapping("/logout")
    private ResponseEntity<?> logout(@RequestHeader("Authorization") String token){
        return ResponseEntity.ok("logout Success");
    }
    

}
