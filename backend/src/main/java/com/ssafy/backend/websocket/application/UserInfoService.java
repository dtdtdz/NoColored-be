package com.ssafy.backend.websocket.application;

import com.ssafy.backend.websocket.dao.UserInfoRepository;
import com.ssafy.backend.websocket.domain.UserInfo;
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

    public UserInfo createAndSaveUserInfo(String userNickname, String userSkin, boolean isGuest, Long userExp, String userTitle, Integer userLevel) {
        UserInfo userInfo = new UserInfo();
        // PrePersist에서 id와 userCode가 설정됩니다.
        userInfo.setUserNickname(userNickname);
        userInfo.setUserSkin(userSkin);
        userInfo.setGuest(isGuest);
        userInfo.setUserExp(userExp);
        userInfo.setUserTitle(userTitle);
        userInfo.setUserLevel(userLevel);

        return userInfoRepository.save(userInfo);
    }
}
