package com.ssafy.backend.user.dto;

import com.ssafy.backend.user.entity.UserProfile;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto {
    String userCode;
    String nickname;
    long exp; //현재 경험치
    long expRequire; //현재 레벨 필요경험치
    int level;
    boolean isGuest;
    int rating;
    String tier;
    int rank;
    String skin;
    String label;
    
    // exp, expRequire, level, 묶어서 계산
    // tier, rank 묶어서 계산
    
    public UserProfileDto(UserProfile userProfile){
        this.userCode = userProfile.getUserCode();
        this.nickname = userProfile.getUserNickname();
//        this.exp = userProfile.getUserExp();
        this.isGuest = userProfile.isGuest();
        this.rating = userProfile.getUserRating();
//        this.tier = "nocolored"; //로직
        this.skin = userProfile.getUserSkin();
        this.label = userProfile.getUserLabel();
    }

    public void calcLevelExp(long exp){
        long currentExp=exp;
        int level=0;
        long reqExp=50;
        while(currentExp>=reqExp){
            currentExp-=reqExp;
            level++;
            if(level<=10){
                reqExp=500;
            }else if(level<=30) {
                reqExp=1000;
            }else if(level<=50) {
                reqExp=1500;
            }else if(level<=75) {
                reqExp=2000;
            }else{
                reqExp=3000;
            }
        }
        this.level = level;
        this.exp = currentExp;
        this.expRequire = reqExp;
    }
}
