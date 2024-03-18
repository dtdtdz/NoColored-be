package com.ssafy.backend.game.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomInfo {

    @Id
    private UUID gameId;
    private String title;
    private String password;
    private int code;
    private int master;
    private UserAccessInfo[] userArr;
    private MapInfo mapInfo;

    // generateRandomString

}
