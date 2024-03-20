package com.ssafy.backend.game.dto;


import com.ssafy.backend.game.domain.GameInfo;
import com.ssafy.backend.game.domain.MapInfo;
import com.ssafy.backend.game.domain.UserAccessInfo;
import com.ssafy.backend.user.dto.UserProfileDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RoomDto {
//    private UUID gameId;//seq로 바꾸는거 생각해보셈 sequential UUID를 쓸까?
    private String title;
    private int password;
    private int code;
    private int master;//방장의 위치.ranking에서는 null
    private int[] readyState;
    private UserProfileDto[] userArr;
    private MapInfo mapInfo;

    public RoomDto(UserAccessInfo user, int mapId, String title, int password){//친선전 생성
        userArr = new UserProfileDto[GameInfo.MAX_PLAYER];
        userArr[0] = new UserProfileDto(user.getUserProfile());
//        gameId = UUID.randomUUID();
        master = 0;
        //mapInfo = new MapInfo(mapId);
        readyState=new int[] {0,0,0,0};
//        userArr= new UserInfoDto[]{null, null, null, null};
        mapInfo = new MapInfo();
        this.title = title;
        this.password = password;
    }

    public RoomDto(List<UserAccessInfo> userList){//랭킹전
        userArr = new UserProfileDto[GameInfo.MAX_PLAYER];
        for (int i=0; i<userList.size(); i++){
            userArr[i] = new UserProfileDto(userList.get(i).getUserProfile());
        }
//        gameId = UUID.randomUUID();
//        master = null;
        mapInfo = new MapInfo();
    }



}
