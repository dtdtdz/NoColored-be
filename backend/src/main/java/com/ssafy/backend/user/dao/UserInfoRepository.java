package com.ssafy.backend.user.dao;

import com.ssafy.backend.user.entity.UserInfo;
import com.ssafy.backend.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserInfoRepository extends JpaRepository<UserInfo, UUID> {
    @Query("select u.userProfile from UserInfo u where userId = :userId and userPwd = :userPwd")
    UserProfile findByUser(@Param("userId")String userId, @Param("userPwd")String userPwd);
}
