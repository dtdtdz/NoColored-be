package com.ssafy.backend.user.dao;

import com.ssafy.backend.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserInfoRepository extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByUserCode(String userCode);

}
