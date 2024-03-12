package com.ssafy.backend.websocket.user.entity;

import com.ssafy.backend.websocket.user.RandomNickname;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "userinfo")
@Getter
@Setter
@Builder
public class UserInfo {

    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(length = 8)
    private String userCode;

    @Column(nullable = false, length = 9)
    private String userNickname;

    @Column(columnDefinition = "LONGTEXT")
    private String userSkin;

    @Column(columnDefinition = "TINYINT(1)")
    private boolean isGuest = false; // 기본값 설정

    @Column(columnDefinition = "DEFAULT 0")
    private Long userExp = 0L; // 기본값 설정

    @Column(length = 30)
    private String userTitle;

    @Column(columnDefinition = "DEFAULT 0")
    private Integer userLevel = 0; // 기본값 설정

    // 엔티티가 영속화되기 전에 필요한 설정
    @PrePersist
    private void userSetting() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (userCode == null) {
            userCode = RandomNickname.generateRandomString();
        }
    }
}
