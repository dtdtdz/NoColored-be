package com.ssafy.backend.play.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FriendlyRoomDto {

    private String roomTitle;
    private String roomCode;
    private int mapId;
    private int userNumber;

}
