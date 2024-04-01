package com.ssafy.backend.user.entity;

import com.ssafy.backend.user.dto.UserProfileDto;
import com.ssafy.backend.user.util.RandomNickname;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "userprofile")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 8, unique = true)
    private String userCode;

    @Column(nullable = false, length = 9)
    private String userNickname;

    private boolean isGuest;

    private Long userExp;

    private String userSkin;

    private String userLabel;

    private Integer userRating;

//    @OneToOne(mappedBy = "userProfile", fetch = FetchType.LAZY)
//    private UserAchievements userAchievements;


}
