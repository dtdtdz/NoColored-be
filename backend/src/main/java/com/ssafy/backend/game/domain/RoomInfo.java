package com.ssafy.backend.game.domain;

import com.ssafy.backend.game.dto.RoomDto;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import lombok.*;

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
