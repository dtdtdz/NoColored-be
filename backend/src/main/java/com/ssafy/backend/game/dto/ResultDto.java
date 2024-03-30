package com.ssafy.backend.game.dto;

import com.ssafy.backend.game.domain.ResultInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class ResultDto {
    UUID roomUuid;
    List<UserResultDto> players;
    public ResultDto(ResultInfo resultInfo){
        players = new LinkedList<>();
        roomUuid = resultInfo.getRoom().getRoomDto().getRoomId();
        players.addAll(resultInfo.getPlayers().values());
    }
}
