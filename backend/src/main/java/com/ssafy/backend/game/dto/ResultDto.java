package com.ssafy.backend.game.dto;

import com.ssafy.backend.game.domain.ResultInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
public class ResultDto {
    UUID roomUuid;
    List<UserResultDto> players;
    RewardDto reward;
    public ResultDto(ResultInfo resultInfo){
        players = new LinkedList<>();
        if (resultInfo.getGameInfo().getRoom()!=null)
            roomUuid = resultInfo.getGameInfo().getRoom().getRoomDto().getRoomId();

        players.addAll(resultInfo.getPlayers().values());
        players.sort(Comparator.comparingInt(o -> o.rank));

        reward = new RewardDto();
    }
}
