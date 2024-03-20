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

    private String title;
    private int password;
    private int code;
    private int master;
    private int[] readyState;
    private UserAccessInfo[] userArr;
    private MapInfo mapInfo;


}
