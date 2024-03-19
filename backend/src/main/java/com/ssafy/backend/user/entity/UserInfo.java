package com.ssafy.backend.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "userinfo", indexes = {
        @Index(columnList = "userId")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserInfo {

    @Id
    private UUID id;

    @Column(length = 30, unique = true)
    private String userId;

    @Column(length = 30)
    private String userPwd;

    @CreatedDate
    private LocalDateTime userCreateDate;
    //default값은 DB에서 직접 지정 권장된다.
    private boolean isDeleted; // 기본값 설정

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // 이를 통해 UserInfo 테이블의 PK를 UserProfile 테이블의 PK와 동일하게 사용
    @JoinColumn(name = "id") // UserInfo 엔티티의 ID 필드를 참조하는 외래 키
    private UserProfile userProfile;

}
