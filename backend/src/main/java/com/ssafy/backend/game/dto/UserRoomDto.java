package com.ssafy.backend.game.dto;

import com.ssafy.backend.user.dto.UserProfileDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserRoomDto {

    private int index;
    private UserProfileDto userProfileDto;
    private boolean isReady;

}
