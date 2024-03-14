package com.ssafy.backend.game.domain;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MapInfo {

    private final int mapId;
    private final int top; //0
    private final int bottom;
    private final int left; //0
    private final int right;

    private final List<int[]> floorList;

    public MapInfo(){
        mapId = 1;
        top = 0;
        bottom = 342;
        left = 54;
        right = 540;//594-54

        floorList = new ArrayList<>();

        floorList.add(new int[] {5,5,6});
        floorList.add(new int[] {15,5,5});
        floorList.add(new int[] {23,5,5});

        floorList.add(new int[] {7,9,8});
        floorList.add(new int[] {20,9,5});

        floorList.add(new int[] {3,13,6});
        floorList.add(new int[] {13,13,7});
        floorList.add(new int[] {24,13,6});

        floorList.add(new int[] {9,17,15});

    }

//    public MapInfo(int mapId){
//        this.mapId = mapId;
//
//        switch (mapId){
//            case 1-> {
//                top = 0;
//                left = 54;
//                right = 540;
//
//            }
//        }
//    }
}
