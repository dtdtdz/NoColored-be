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

    String nickname;
    String label;
    int index;
    String skin;
    int rank;
    int step;

    public UserResultDto(UserAccessInfo userAccessInfo){
        nickname = userAccessInfo.getUserProfile().getUserNickname();
        label = userAccessInfo.getUserProfile().getUserLabel();
        skin = userAccessInfo.getUserProfile().getUserSkin();
        index = userAccessInfo.getGameInfo().getUsers().get(userAccessInfo).getPlayerNum();
        rank = userAccessInfo.getGameInfo().getUsers().get(userAccessInfo).getUserPlayInfo().getRank();
        step = userAccessInfo.getGameInfo().getUsers().get(userAccessInfo).getUserPlayInfo().getStep();
    }

}
