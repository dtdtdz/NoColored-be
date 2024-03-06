package com.ssafy.backend.websocket.domain;

import lombok.Getter;

import java.util.Map;

@Getter
public class MapInfo {

    private int top; //0
    private int bottom;
    private int left; //0
    private int right;



    public MapInfo(){
        top = 0;
        bottom = 600;
        left = 0;
        right = 800;
    }
}
