package com.ssafy.backend.user.dao;

import com.ssafy.backend.user.entity.UserProfile;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    boolean existsByUserCode(String userCode);

    @Transactional
    @Modifying
    @Query("update UserProfile set userNickname = :userNickname where id = :id")
    void updateNickname(@Param("id")UUID id, @Param("userNickname") String userNickname);

    Optional<UserProfile> findByUserCode(String userCode);

}
