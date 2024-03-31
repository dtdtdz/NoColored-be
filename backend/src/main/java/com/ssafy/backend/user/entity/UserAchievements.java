package com.ssafy.backend.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "userachievements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAchievements {

    @Id
    private UUID id;

    private String userCode;

    private LocalDateTime lastLoginDate;
    private int consecutiveLoginDays; // 연속접속수
    private int cumulativeLoginDays; // 누적접속수
    private boolean isConsecutiveLogin; // 연속 로그인 끊김 확인

    private int cumulativePlayCount; // 누적 플레이 수
    private int cumulativeWinCount; // 누적 승수
    private int cumulativeLoseCount; // 누적 패수
    private long playtime; // 누적 플레이타임(분)

    // 아이템 획득 수 5개 넣어놓기




    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // 이를 통해 테이블의 PK를 UserProfile 테이블의 PK와 동일하게 사용
    @JoinColumn(name = "id") // ID 필드를 참조하는 외래 키
    private UserProfile userProfile;

}
