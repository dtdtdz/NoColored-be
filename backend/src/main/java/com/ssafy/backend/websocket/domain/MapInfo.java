package com.ssafy.backend.websocket.domain;

import lombok.Getter;

import java.util.Map;

@Getter
public class MapInfo {

    private final int top; //0
    private final int bottom;
    private final int left; //0
    private final int right;



    public MapInfo(){
        top = 0;
        bottom = 342;
        left = 54;
        right = 540;
    }
}
