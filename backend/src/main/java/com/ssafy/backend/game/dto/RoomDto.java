package com.ssafy.backend.game.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomDto {
//    private UUID gameId;//seq로 바꾸는거 생각해보셈 sequential UUID를 쓸까?
    private String roomTitle;
    private String roomCodeString;
    private int masterIndex; // 방장의 위치.ranking에서는 null
    private String roomPassword;
    private UserRoomDto[] players;
    private int mapId;

//    public RoomDto(UserAccessInfo user, int mapId, String title){//친선전 생성
//        userArr = new UserProfileDto[GameInfo.MAX_PLAYER];
//        userArr[0] = new UserProfileDto(user.getUserProfile());
////        gameId = UUID.randomUUID();
//        master = 0;
//        //mapInfo = new MapInfo(mapId);
//        readyState=new int[] {0,0,0,0};
////        userArr= new UserInfoDto[]{null, null, null, null};
//        mapInfo = new MapInfo();
//        this.title = title;
//    }
//
//    public RoomDto(List<UserAccessInfo> userList){//랭킹전
//        userArr = new UserProfileDto[GameInfo.MAX_PLAYER];
//        for (int i=0; i<userList.size(); i++){
//            userArr[i] = new UserProfileDto(userList.get(i).getUserProfile());
//        }
////        gameId = UUID.randomUUID();
////        master = null;
//        mapInfo = new MapInfo();
//    }



}
