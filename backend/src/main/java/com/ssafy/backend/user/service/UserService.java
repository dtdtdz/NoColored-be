package com.ssafy.backend.user.service;

import com.ssafy.backend.user.entity.UserProfile;

public interface UserService {
    String signUp(String id, String password, String nickname);
    public String generateToken(String userCode);
}
