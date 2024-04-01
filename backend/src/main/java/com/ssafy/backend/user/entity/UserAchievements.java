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

    private int step; // 누적 밟음
    private int stepped; // 누적 밟힘

    // 아이템 획득 수 5개 넣어놓기
    private int lightUPallCount;
    private int stopNPCCount;
    private int randomBoxCount;
    private int rebelCount;
    private int stopPlayerCount;

    private int itemCount; // 아이템 먹은 수

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // 이를 통해 테이블의 PK를 UserProfile 테이블의 PK와 동일하게 사용
    @JoinColumn(name = "id") // ID 필드를 참조하는 외래 키
    private UserProfile userProfile;

    public UserAchievements(UserAchievements userAchievements) {
        this.id = userAchievements.getId();
        this.userCode = userAchievements.getUserCode();
        this.lastLoginDate = userAchievements.getLastLoginDate();
        this.consecutiveLoginDays = userAchievements.getConsecutiveLoginDays();
        this.cumulativeLoginDays = userAchievements.getCumulativeLoginDays();
        this.isConsecutiveLogin = userAchievements.isConsecutiveLogin();
        this.cumulativePlayCount = userAchievements.getCumulativePlayCount();
        this.cumulativeWinCount = userAchievements.getCumulativeWinCount();
        this.cumulativeLoseCount = userAchievements.getCumulativeLoseCount();
        this.playtime = userAchievements.getPlaytime();
        this.step=userAchievements.getStep();
        this.stepped=userAchievements.getStepped();
        this.lightUPallCount = userAchievements.getLightUPallCount();
        this.stopNPCCount = userAchievements.getStopNPCCount();
        this.randomBoxCount = userAchievements.getRandomBoxCount();
        this.rebelCount = userAchievements.getRebelCount();
        this.stopPlayerCount = userAchievements.getStopPlayerCount();
        this.itemCount=userAchievements.getItemCount();
        this.userProfile=null;
    }

}
