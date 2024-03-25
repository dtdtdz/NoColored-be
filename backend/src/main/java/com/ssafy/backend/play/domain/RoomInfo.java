package com.ssafy.backend.play.domain;

import com.ssafy.backend.game.domain.MapInfo;
import com.ssafy.backend.play.dto.RoomDto;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import lombok.*;

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
//    private MapInfo mapInfo;
}
