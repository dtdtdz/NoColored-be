package com.ssafy.backend.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "userprofile")
@Getter
@Setter
@Builder
public class UserProfile {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(length = 30)
    private String userId;

    @Column(length = 30)
    private String userPwd;

    @Column
    private LocalDateTime userCreateDate;

    @Column
    private boolean isDelete = false; // 기본값 설정

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // 이를 통해 UserInfo 테이블의 PK를 UserProfile 테이블의 PK와 동일하게 사용
    @JoinColumn(name = "id") // UserInfo 엔티티의 ID 필드를 참조하는 외래 키
    private UserInfo userInfo;
}
