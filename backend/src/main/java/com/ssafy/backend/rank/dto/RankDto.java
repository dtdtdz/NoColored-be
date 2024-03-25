package com.ssafy.backend.rank.dto;

import com.ssafy.backend.user.entity.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RankDto {
    int rank;
    String userCode;
    String nickname;
    int rating;
    String skin;
    String title;
    String tier;

    public RankDto(UserProfile userProfile){
        rank = 1;
        userCode = "Umr0Lztd";
        nickname = "편안한 곰";
        rating = 1000;
        tier = "origin";
        title = "";
        skin = "";
    }
}
