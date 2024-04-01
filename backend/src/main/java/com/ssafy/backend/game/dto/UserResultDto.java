package com.ssafy.backend.game.dto;

import com.ssafy.backend.game.domain.GameInfo;
import com.ssafy.backend.game.domain.UserGameInfo;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserResultDto {

    String userCode;
    String nickname;
    String label;
    int index;
    String skin;
    int rank;
    int score;

    public UserResultDto(UserAccessInfo userAccessInfo){
        userCode=userAccessInfo.getUserProfile().getUserCode();
        nickname = userAccessInfo.getUserProfile().getUserNickname();
        label = userAccessInfo.getUserProfile().getUserLabel();
        skin = userAccessInfo.getUserProfile().getUserSkin();
        index = userAccessInfo.getGameInfo().getUsers().get(userAccessInfo).getPlayerNum();
        rank = userAccessInfo.getGameInfo().getUsers().get(userAccessInfo).getUserPlayInfo().getRank();
        score = userAccessInfo.getGameInfo().getUsers().get(userAccessInfo).getScore();
    }

}
