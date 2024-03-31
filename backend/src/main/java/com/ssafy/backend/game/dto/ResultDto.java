package com.ssafy.backend.game.dto;

import com.ssafy.backend.game.domain.ResultInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ResultDto {
    UUID roomUuid;
    List<UserResultDto> players;
    RewardDto reward;
    public ResultDto(ResultInfo resultInfo){
        players = new LinkedList<>();
        if (resultInfo.getRoom()!=null) roomUuid = resultInfo.getRoom().getRoomDto().getRoomId();
        players.addAll(resultInfo.getPlayers().values());
        reward = new RewardDto();
    }
}
