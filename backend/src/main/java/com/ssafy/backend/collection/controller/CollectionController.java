package com.ssafy.backend.collection.controller;


import com.ssafy.backend.collection.service.CollectionService;
import com.ssafy.backend.user.util.JwtUtil;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
/**
 * 유저의 컬렉션(스킨, 칭호, 업적) 정보를 불러오고, 유저의 칭호, 스킨을 변경하기 위한 컨트롤러
 * getCollection, changeSkin, changeLabel만 클라이언트에서 호출됨
 */
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
    @PostMapping("/add/label/{labelId}")
    public ResponseEntity<?> addLabelCollection(@RequestHeader("Authorization") String token, @PathVariable int labelId){
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        return collectionService.addLabel(userAccessInfo,labelId);
    }
    @PostMapping("/add/achievement/{achievementId}")
    public ResponseEntity<?> addAchievementCollection(@RequestHeader("Authorization") String token, @PathVariable int achievementId){
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        return collectionService.addAchievement(userAccessInfo, achievementId);
    }
    
    // 몽고에 데이터 넣기
    @PostMapping("/put/skin")
    public ResponseEntity<?> putSkinCollection(){
        return collectionService.putSkin();
    }
    @PostMapping("/put/label")
    public ResponseEntity<?> putLabelCollection(){
        return collectionService.putLabel();
    }
    @PostMapping("/put/achievement")
    public ResponseEntity<?> putAchievementCollection(){
        return collectionService.putAchievement();
    }
    
    // 몽고에 있는 데이터 삭제
    @DeleteMapping("/delete/skin")
    public ResponseEntity<?> deleteSkinCollection(){
        return collectionService.deleteSkin();
    }
    @DeleteMapping("/delete/label")
    public ResponseEntity<?> deleteLabelCollection(){
        return collectionService.deleteLabel();
    }
    @DeleteMapping("/delete/achievement")
    public ResponseEntity<?> deleteAchievementCollection(){
        return collectionService.deleteAchievement();
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

    @PatchMapping("/label")
    public ResponseEntity<?> changeLabel(@RequestHeader("Authorization") String token, @RequestBody Map<String, Integer> requestBody){
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        Integer labelId=requestBody.get("labelId");
        return collectionService.changeLabel(userAccessInfo,labelId);
    }


}
