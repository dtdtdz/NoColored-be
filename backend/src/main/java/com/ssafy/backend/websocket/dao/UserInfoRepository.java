package com.ssafy.backend.websocket.dao;

import com.ssafy.backend.websocket.domain.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserInfoRepository extends JpaRepository<UserInfo, UUID> {
    Optional<UserInfo> findByUserCode(String userCode);

}
