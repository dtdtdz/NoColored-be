package com.ssafy.backend.user.dto;

import com.ssafy.backend.user.entity.UserProfile;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto {
    String token;//이건 로그인 후 자기정보일때만 not null
    String userCode;
    String nickName;
    long exp; //현재 경험치
    long expRequire; //현재 레벨 필요경험치
    int level;
    boolean isGuest;
    int rating;
    String tier;
    String skin;
    String title;
    public UserProfileDto(UserProfile userProfile){
        this.userCode = userProfile.getUserCode();
        this.nickName = userProfile.getUserNickname();
        this.exp = userProfile.getUserExp();
        this.isGuest = userProfile.isGuest();
        this.rating = userProfile.getUserRating();
        this.tier = "nocolored"; //로직
        this.skin = userProfile.getUserSkin();
        this.title = userProfile.getUserTitle();
    }
    public UserProfileDto(UserProfile userProfile, String token){
        this(userProfile);
        this.token = token;
    }
}
