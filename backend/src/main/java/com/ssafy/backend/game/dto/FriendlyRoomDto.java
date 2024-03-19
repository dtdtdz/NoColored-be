package com.ssafy.backend.game.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FriendlyRoomDto {

    private String roomTitle;
    private int roomCode;
    private int mapId;
    private int userNumber;

}
