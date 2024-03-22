package com.ssafy.backend.user.service;

import com.ssafy.backend.user.dto.UserProfileDto;
import com.ssafy.backend.user.dto.UserSignDto;
import com.ssafy.backend.user.entity.UserProfile;

public interface UserService {
    UserProfileDto getUserProfileDto(String token);
    UserProfile guestSignUp();
    String guestConvert(String token, UserSignDto userSignDto);
    UserProfile signUp(String id, String password, String nickname);
    String generateToken(UserProfile userProfile);
    String login(String id, String password);
    void updatePassword(String token, String pwd, String prePwd);
    void updateNickname(String token, String nickname);
    void deleteUser(String token, String prePwd);
    boolean existsUserId(String userId);
    UserProfileDto findUserInfo(String userCode);
}
