package com.ssafy.backend.websocket.domain;

import lombok.Getter;

import java.util.Map;

@Getter
public class MapInfo {

    private final int top; //0
    private final int bottom;
    private final int left; //0
    private final int right;

    private final boolean[][] floor;

    public MapInfo(){
        top = 0;
        bottom = 342;
        left = 54;
        right = 540;//594-54
        floor = new boolean[27][19];
        for (int i=6; i<floor.length-6; i++){
            floor[i][17] = true;
        }

        for (int i=10; i<17; i++){
            floor[i][13] = true;
        }
    }
}
