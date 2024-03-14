package com.ssafy.backend.game.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CharacterInfo {
    private float x;
    private float y;
    private float velX;
    private float velY;
    private UserGameInfo userGameInfo;
    private boolean jump;

    //속도와 방향을 따로 설정할까?
    public CharacterInfo(float x, float y){
        this.x = x;
        this.y = y;
        velX = 160f;
        velY = 0;
        userGameInfo = null;
        jump = false;
    }

}
