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
    private GameInfo gameInfo;
//    enum state 필요?
    public UserAccessInfo(UserProfile userProfile){
        this.userProfile = userProfile;
    }

}
