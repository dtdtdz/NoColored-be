package com.ssafy.backend.game.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FriendlyRoomDto {

    private String title;
    private int code;
    private int mapId;
    private int userNumber;

}
