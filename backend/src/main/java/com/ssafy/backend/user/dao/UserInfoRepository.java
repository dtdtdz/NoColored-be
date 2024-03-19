package com.ssafy.backend.user.dao;

import com.ssafy.backend.user.entity.UserInfo;
import com.ssafy.backend.user.entity.UserProfile;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserInfoRepository extends JpaRepository<UserInfo, UUID> {
    @Query("select u.userProfile from UserInfo u where userId = :userId and userPwd = :userPwd")
    UserProfile findByUser(@Param("userId")String userId, @Param("userPwd")String userPwd);

    @Transactional
    @Modifying
    @Query("update UserInfo set userProfile.userNickname = :userNickname where id = :id")
    void updateNickname(@Param("id")UUID id, @Param("userNickname") String userNickname);

    @Transactional
    @Modifying
    @Query("update UserInfo set userPwd = :userPwd where id = :id")
    void updatePassword(@Param("id")UUID id, @Param("userPwd") String userPwd);

    @Transactional
    @Modifying
    @Query("update UserInfo set isDeleted = true where id = :id")
    void deleteUser(@Param("id")UUID id);

    String findUserPwdById(UUID id);
}
