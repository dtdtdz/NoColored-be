package com.ssafy.backend.game.dto;

import com.ssafy.backend.user.dto.UserProfileDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRoomDto {

    private int userIndex;
    private UserProfileDto player;
    private boolean isReady;

}
