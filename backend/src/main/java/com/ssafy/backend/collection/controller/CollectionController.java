package com.ssafy.backend.collection.controller;


import com.ssafy.backend.collection.dao.UserCollection;
import com.ssafy.backend.collection.service.CollectionService;
import com.ssafy.backend.user.dto.UserProfileDto;
import com.ssafy.backend.user.util.JwtUtil;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/collection")
public class CollectionController {

    @Autowired
    private CollectionService collectionService;
    @Autowired
    private JwtUtil jwtUtil;

    // 유저한테 데이터 넣기
    @PostMapping("/add/skin/{skinId}")
    public ResponseEntity<?> addSkinCollection(@RequestHeader("Authorization") String token, @PathVariable int skinId){
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        return collectionService.addSkin(userAccessInfo,skinId);
    }
    @PostMapping("/add/title")
    public ResponseEntity<?> addTitleCollection(@RequestHeader("Authorization") String token){
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        return collectionService.addTitle();
    }
    @PostMapping("/add/achievement")
    public ResponseEntity<?> addAchievementCollection(@RequestHeader("Authorization") String token){
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        return collectionService.addAchievement();
    }
    
    // 테스트용 더미 데이터 넣기
    @PostMapping("/put/skin")
    public ResponseEntity<?> putSkinCollection(){
        return collectionService.putSkin();
    }
    @PostMapping("/put/title")
    public ResponseEntity<?> putTitleCollection(){
        return collectionService.putTitle();
    }
    @PostMapping("/put/achievement")
    public ResponseEntity<?> putAchievementCollection(){
        return collectionService.putAchievement();
    }

    @GetMapping
    public ResponseEntity<?> getCollection(@RequestHeader("Authorization") String token){
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        return collectionService.getCollections(userAccessInfo);
    }

    @PatchMapping("/skin")
    public ResponseEntity<?> changeSkin(@RequestHeader("Authorization") String token, @RequestBody Map<String, Integer> requestBody){
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        Integer skinId=requestBody.get("skinId");
        return collectionService.changeSkin(userAccessInfo,skinId);
    }

    @PatchMapping("/title")
    public ResponseEntity<?> changeTitle(@RequestHeader("Authorization") String token, @RequestBody Map<String, Integer> requestBody){
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        Integer titleId=requestBody.get("titleId");
        return collectionService.changeTitle(userAccessInfo,titleId);
    }


}
