package com.ssafy.backend.websocket.user.service;

import com.ssafy.backend.websocket.user.repository.UserInfoRepository;
import com.ssafy.backend.websocket.user.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserInfoService {

    private final UserInfoRepository userInfoRepository;

    @Autowired
    public UserInfoService(UserInfoRepository userInfoRepository) {
        this.userInfoRepository = userInfoRepository;
    }

    Optional<UserInfo> findUserInfoByUserCode(String userCode){
        return userInfoRepository.findByUserCode(userCode);
    }

    public boolean existsUserByUserCode(String userCode) {
        return findUserInfoByUserCode(userCode).isPresent();
    }
}
