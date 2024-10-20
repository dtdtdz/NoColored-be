package com.ssafy.backend.user.service;

import com.ssafy.backend.user.dto.UserProfileDto;
import com.ssafy.backend.user.dto.UserSignDto;
import com.ssafy.backend.user.entity.UserProfile;
import com.ssafy.backend.websocket.domain.UserAccessInfo;

public interface UserService {
    UserProfileDto getUserProfileDto(String token);
    UserAccessInfo guestSignUp();
    String guestConvert(String token, UserSignDto userSignDto);
    UserAccessInfo signUp(String id, String password, String nickname);
    String generateToken(UserAccessInfo userProfile);
    String login(String id, String password);
    void updatePassword(String token, String pwd, String prePwd);
    void updateNickname(String token, String nickname);
    boolean confirmUser(String token, String password);
    void deleteUser(String token);
    boolean existsUserId(String userId);
    UserProfileDto findUserInfo(String userCode);
    void activeLogout(String token);
    boolean isTokenValid(String token);
}
