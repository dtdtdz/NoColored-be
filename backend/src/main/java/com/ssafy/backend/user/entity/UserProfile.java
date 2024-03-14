package com.ssafy.backend.user.entity;

import com.ssafy.backend.user.util.RandomNickname;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "userprofile")
@Getter
@Setter
@Builder
public class UserProfile {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 8, unique = true)
    private String userCode;

    @Column(nullable = false, length = 9)
    private String userNickname;

    @Column(columnDefinition = "LONGTEXT")//url, 변경될 수 있음
    private String userSkin;

    private boolean isGuest = false; // 기본값 설정

    private Long userExp = 0L; // 기본값 설정

    @Column(length = 30)
    private String userTitle;

    private Integer userLevel = 0; // 기본값 설정

}
