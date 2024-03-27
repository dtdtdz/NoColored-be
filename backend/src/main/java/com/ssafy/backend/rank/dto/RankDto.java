package com.ssafy.backend.rank.dto;

import com.ssafy.backend.user.entity.UserProfile;
import lombok.*;
import org.springframework.web.bind.annotation.GetMapping;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankDto {
    int rank;
    String userCode;
    String nickname;
    int rating;
    String skin;
    String label;
    String tier;
}
