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


/**
 * 유저 관리 및 추적을 위한 클래스
 * 유저와 매핑된 웹소켓 세션 관리
 * 유저는 로그아웃 요청없이 서비스에서 나갈 수 있음 -> expireTime(1시간)으로 유효성 확인
 * 유저의 웹페이지 이동이 자유로우므로, 게임과 관련된 요청은 적절한 위치에서만 이뤄져야함
 * 유저의 위치(position) 매칭, 친선전 대기실, 게임, 결과창, 기본값으로 나뉨
 * 위치가 잘못된 요청을 무효화
 * 유저 정보를 저장 -> Redis에 캐싱하는 방법 고려
 */
@Getter
@Setter
@NoArgsConstructor
public class UserAccessInfo {
    private WebSocketSession session;
    private Object position;//사용할때 상태 확인해야함

    private long expireTime;

    private static long USER_EXPIRE = 1000*3600;

    private UserProfile userProfile;
    private UserProfileDto userProfileDto;//Dto 객체 생성비용을 아끼기 위해서 Dto를 저장하는건 나쁜 생각이었다
    private UserAchievements userAchievements;
    private UserCollection userCollection;



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
