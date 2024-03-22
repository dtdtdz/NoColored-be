package com.ssafy.backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayUserDto {
    String userCode;
    String nickName;
    String tier;
    String skin;
    String title;
}
