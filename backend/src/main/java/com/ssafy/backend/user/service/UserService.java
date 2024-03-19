package com.ssafy.backend.user.service;

import com.ssafy.backend.user.dto.UserInfoDto;
import com.ssafy.backend.user.dto.UserSignDto;
import com.ssafy.backend.user.entity.UserProfile;

public interface UserService {
    UserProfile guestSignUp();
    UserInfoDto guestConvert(String token, UserSignDto userSignDto);
    UserProfile signUp(String id, String password, String nickname);
    UserInfoDto generateUserInfoDtoWithToken(UserProfile userProfile);
    UserInfoDto login(String id, String password);
    void updatePassword(String token, String pwd, String prePwd);
    void updateNickname(String token, String nickname);
    void deleteUser(String token, String prePwd);

}
