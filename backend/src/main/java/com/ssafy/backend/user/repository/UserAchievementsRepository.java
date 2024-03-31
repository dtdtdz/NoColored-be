package com.ssafy.backend.user.repository;

import com.ssafy.backend.user.entity.UserAchievements;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserAchievementsRepository extends JpaRepository<UserAchievements, UUID> {
    UserAchievements findByUserCode(String userCode);
}
