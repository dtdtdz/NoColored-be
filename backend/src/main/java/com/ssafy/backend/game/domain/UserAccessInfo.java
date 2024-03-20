package com.ssafy.backend.game.domain;

import com.ssafy.backend.user.entity.UserProfile;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;


@Getter
@Setter
@NoArgsConstructor
public class UserAccessInfo {
    private WebSocketSession session;
    private UserProfile userProfile;
    private Object position;//사용할때 상태 확인해야함

    public UserAccessInfo(UserProfile userProfile){
        this.userProfile = userProfile;
    }

    public RoomInfo getRoomInfo(){
       if (position instanceof RoomInfo) return (RoomInfo) position;
       else return null;
    }

    public GameInfo getGameInfo(){
        if (position instanceof GameInfo) return (GameInfo) position;
        else return null;
    }

    public void setGameInfo(GameInfo gameInfo){
        position = gameInfo;
    }

    public void setRoomInfo(RoomInfo roomInfo){
        position = roomInfo;
    }

    public void clearPosition(){
        position = null;
    }
}
