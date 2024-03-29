package com.ssafy.backend.game.domain;

import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
public class MapInfo {

    private final int mapId;
    private final int top; //0
    private final int bottom;
    private final int left; //0
    private final int right;

    private final List<int[]> floorList;
    private static List<List<int[]>> MapList;

    static {
        List<int[]> list1 = new LinkedList<>();
        list1.add(new int[] {5,5,6});
        list1.add(new int[] {15,5,5});
        list1.add(new int[] {23,5,5});

        list1.add(new int[] {7,9,8});
        list1.add(new int[] {20,9,5});

        list1.add(new int[] {3,13,6});
        list1.add(new int[] {13,13,7});
        list1.add(new int[] {24,13,6});

        list1.add(new int[] {9,17,15});
        MapList.add(list1);

        List<int[]> list2 = new LinkedList<>();
        list2.add(new int[]{3,5,6});
        list2.add(new int[]{10,5,4});
        list2.add(new int[]{17,5,6});
        list2.add(new int[]{26,5,4});

        list2.add(new int[]{3,9,3});
        list2.add(new int[]{9,9,6});
        list2.add(new int[]{19,9,6});

        list2.add(new int[]{3,13,6});
        list2.add(new int[]{13,13,6});
        list2.add(new int[]{24,13,6});
        MapList.add(list2);
    }

    public MapInfo(int mapId){
        this.mapId = mapId;
        top = 0;
        bottom = 342;
        left = 54;
        right = 540;//594-54

        floorList = MapList.get(mapId);
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
