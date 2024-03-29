package com.ssafy.backend.game.domain;

import com.ssafy.backend.game.dto.Reward;
import com.ssafy.backend.game.dto.UserResultDto;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
public class ResultInfo {
    private Map<UserAccessInfo,UserResultDto> players;
    Reward reward;
    UUID roomUuid;
    public ResultInfo(GameInfo gameInfo){
        players = new LinkedHashMap<>();
        roomUuid = gameInfo.getRoomUuid();
        for (UserAccessInfo userAccessInfo:gameInfo.getUsers().keySet()){
            players.put(userAccessInfo ,new UserResultDto(userAccessInfo));
        }

    }
}
