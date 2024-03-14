package com.ssafy.backend.user.service;

import com.ssafy.backend.user.entity.UserProfile;

import java.util.UUID;

public interface UserService {
    UserProfile guestSignUp();
    UserProfile signUp(String id, String password, String nickname);
    public String generateToken(UserProfile userProfile);
    public String login(String id, String password);
}
