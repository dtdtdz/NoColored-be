package com.ssafy.backend.user.dto;

import com.ssafy.backend.user.entity.UserProfile;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDto {
    String token;//이건 로그인 후 자기정보일때만 not null
    String userCode;
    String nickName;
    long exp; //누적 경험치? 남은 경험치? 둘다?
    int level;
    String tier;
    String skinId;
    String titleId;
    public UserInfoDto(UserProfile userProfile){
        this.userCode = userProfile.getUserCode();
        this.nickName = userProfile.getUserNickname();
        this.exp = userProfile.getUserExp();
        this.tier = null; //로직
        this.skinId = userProfile.getUserSkin();
        this.titleId = userProfile.getUserTitle();
    }
    public UserInfoDto(UserProfile userProfile, String token){
        this.token = token;
        this.userCode = userProfile.getUserCode();
        this.nickName = userProfile.getUserNickname();
        this.exp = userProfile.getUserExp();
        this.tier = null; //로직
        this.skinId = userProfile.getUserSkin();
        this.titleId = userProfile.getUserTitle();
    }
}
