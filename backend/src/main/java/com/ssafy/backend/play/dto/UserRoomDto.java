package com.ssafy.backend.play.dto;

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

    String userCode;
    String nickname;
    String tier;
    String skin;
    String title;
    boolean isReady;

    public void setEmptyUser(){
        userCode = nickname = tier = skin = title = "";
    }
    public void setUser(UserProfileDto userProfileDto){
        userCode = userProfileDto.getUserCode();
        nickname = userProfileDto.getNickname();
        tier = userProfileDto.getTier();
        skin = userProfileDto.getSkin();
        title = userProfileDto.getTitle();
        isReady = false;
    }

}
