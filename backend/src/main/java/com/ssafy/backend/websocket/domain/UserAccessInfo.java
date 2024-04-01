package com.ssafy.backend.websocket.domain;

import com.ssafy.backend.collection.document.UserCollection;
import com.ssafy.backend.game.domain.GameInfo;
import com.ssafy.backend.game.domain.ResultInfo;
import com.ssafy.backend.play.domain.MatchingInfo;
import com.ssafy.backend.play.domain.RoomInfo;
import com.ssafy.backend.user.dto.UserProfileDto;
import com.ssafy.backend.user.entity.UserAchievements;
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
    private UserProfileDto userProfileDto;
    private Object position;//사용할때 상태 확인해야함
    private UserAchievements userAchievements;
    private UserCollection userCollection;
    private long expireTime;

    private static long USER_EXPIRE = 1000*3600;


    public UserAccessInfo(UserProfile userProfile){
        this.userProfile = userProfile;
        setExpireTime();
    }




    public void setExpireTime() {
        expireTime = System.currentTimeMillis() + USER_EXPIRE;
    }

    public boolean isExpire(){
        return (System.currentTimeMillis()>expireTime);
    }

    public RoomInfo getRoomInfo(){
       if (position instanceof RoomInfo) return (RoomInfo) position;
       else return null;
    }

    public GameInfo getGameInfo(){
        if (position instanceof GameInfo) return (GameInfo) position;
        else return null;
    }

    public ResultInfo getResultInfo(){
        if (position instanceof ResultInfo) return (ResultInfo) position;
        else return null;
    }

    public void setGameInfo(GameInfo gameInfo){
        if (!(position instanceof RoomInfo || position instanceof MatchingInfo))
            throw new RuntimeException("Position conflict"+position.getClass());
        position = gameInfo;
    }

    public void setMatchingInfo(MatchingInfo matchingInfo){
        if (position instanceof MatchingInfo || position instanceof RoomInfo || position instanceof GameInfo)
            throw new RuntimeException("Position conflict"+position.getClass());
        position = matchingInfo;
    }
    public void setRoomInfo(RoomInfo roomInfo){
        if (position instanceof MatchingInfo || position instanceof RoomInfo || position instanceof GameInfo)
            throw new RuntimeException("Position conflict"+position.getClass());
        position = roomInfo;
    }

    public void  setResultInfo(ResultInfo resultInfo){
        if (!(position instanceof GameInfo))
            throw new RuntimeException("Position conflict"+position.getClass());
        position = resultInfo;
    }

    public void clearPosition(){
        position = null;
    }
}
