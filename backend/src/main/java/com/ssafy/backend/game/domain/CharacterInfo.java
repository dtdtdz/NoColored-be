package com.ssafy.backend.game.domain;

import com.ssafy.backend.game.type.GameUserState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CharacterInfo {
    private float x;
    private float y;
    private float velX;
    private float velY;
    private int dir;//방향
    private UserGameInfo userGameInfo;
    private boolean jump;
    private Map<GameUserState, Long> states;//milliSecond

    //속도와 방향을 따로 설정할까?
    public CharacterInfo(float x, float y){
        this.x = x;
        this.y = y;
        velX = 160f;
        velY = 0;
        userGameInfo = null;
        jump = false;
        states = new LinkedHashMap<>();
    }

}
