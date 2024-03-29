package com.ssafy.backend.game.controller;

import com.ssafy.backend.game.domain.GameRoomDto;
import com.ssafy.backend.game.domain.ResultInfo;
import com.ssafy.backend.game.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/ingame")
public class InGameController {

    private final GameService gameService;
    public InGameController(GameService gameService){
        this.gameService = gameService;
    }

    @GetMapping("/ready")
    private ResponseEntity<?> ready(@RequestHeader("Authorization") String token){
        GameRoomDto gameRoomDto = gameService.ready(token);
        if (gameRoomDto==null){
            return ResponseEntity.internalServerError().body("Game containing the player not found");
        }

        return ResponseEntity.ok(gameRoomDto);
    }

    @GetMapping("/ready/dummy")
    private ResponseEntity<?> readyDummy(@RequestHeader("Authorization") String token){
        GameRoomDto gameRoomDto = new GameRoomDto();
        gameRoomDto.setMapId(1);
        List<String> list = new LinkedList<>();
        list.add("https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-basicblue-butterfly.png");
        list.add("https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-basicblue-magichat.png");
        gameRoomDto.setSkins(list);

        List<int[]> list2 = new LinkedList<>();
        list2.add(new int[]{5,5,6});
        list2.add(new int[]{15,5,5});
        list2.add(new int[]{23,5,5});
        list2.add(new int[]{7,9,8});
        list2.add(new int[]{20,9,5});
        list2.add(new int[]{3,13,6});
        list2.add(new int[]{13,13,7});
        list2.add(new int[]{24,13,6});
        list2.add(new int[]{9,17,15});

        gameRoomDto.setFloorList(list2);
//        if (gameRoomDto!=null){
//            return ResponseEntity.internalServerError().body("Game containing the player not found");
//        }

        return ResponseEntity.ok(gameRoomDto);
    }

    // 플레이어마다 다른 resultdto
    @GetMapping
    public ResponseEntity<?> getGameResultData(@RequestHeader("Authorization") String token){

        return ResponseEntity.ok(gameService.getResult(token));
    }

}
