package com.ssafy.backend.game.domain;

import com.ssafy.backend.game.dto.RoomDto;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomInfo {

    private UserAccessInfo[] userAccessInfos;
    private int roomCodeInt;
    private RoomDto roomDto;
    private boolean isGameStart;
    private MapInfo mapInfo;
}
