package com.ssafy.backend.game.dto;


import com.ssafy.backend.game.domain.GameInfo;
import com.ssafy.backend.game.domain.MapInfo;
import com.ssafy.backend.game.domain.UserAccessInfo;
import com.ssafy.backend.user.dto.UserInfoDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class RoomDto {
    private UUID gameId;//seq로 바꾸는거 생각해보셈 sequential UUID를 쓸까?
    private String title;
    private String password;
    private int code;
    private int master;//방장의 위치.ranking에서는 null
    //1p(0번째)가 방장
    private UserInfoDto[] userArr;
    private MapInfo mapInfo;

    public RoomDto(UserAccessInfo user, int mapId, String title, String password){//친선전
        userArr = new UserInfoDto[GameInfo.MAX_PLAYER];
        userArr[0] = new UserInfoDto(user.getUserProfile());
        gameId = UUID.randomUUID();
        master = 0;
        //mapInfo = new MapInfo(mapId);
        mapInfo = new MapInfo();
        this.title = title;
        this.password = password;
    }

    public RoomDto(List<UserAccessInfo> userList){//랭킹전
        userArr = new UserInfoDto[GameInfo.MAX_PLAYER];
        for (int i=0; i<userList.size(); i++){
            userArr[i] = new UserInfoDto(userList.get(i).getUserProfile());
        }
        gameId = UUID.randomUUID();
//        master = null;
        mapInfo = new MapInfo();
    }
}
