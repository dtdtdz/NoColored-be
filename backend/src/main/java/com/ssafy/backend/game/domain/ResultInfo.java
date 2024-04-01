package com.ssafy.backend.game.domain;

import com.ssafy.backend.game.dto.RewardDto;
import com.ssafy.backend.game.dto.UserResultDto;
import com.ssafy.backend.user.entity.UserAchievements;
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
    RewardDto reward;
    GameInfo gameInfo;
    // 딥카피 여기로 옮기기
    private UserAchievements deepCopyOfAchievements; // 깊은 복사본 필드 추가

    public ResultInfo(GameInfo gameInfo){
        players = new LinkedHashMap<>();
        this.gameInfo = gameInfo;
        if (this.gameInfo.getRoom() !=null) this.gameInfo.getRoom().setGameStart(false);
        for (UserAccessInfo userAccessInfo:gameInfo.getUsers().keySet()){
            players.put(userAccessInfo ,new UserResultDto(userAccessInfo));
        }

    }
}
