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
    String title;
    String tier;
    // 레디스에 상위 100개만 캐싱하기
    // 상위 유저가 변경 요청했을때 적용이되는 로직이 필요함
}
