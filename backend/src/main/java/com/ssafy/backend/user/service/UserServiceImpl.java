package com.ssafy.backend.user.service;

import com.ssafy.backend.user.dao.UserInfoRepository;
import com.ssafy.backend.user.entity.UserInfo;
import com.ssafy.backend.user.entity.UserProfile;
import com.ssafy.backend.user.dao.UserProfileRepository;
import com.ssafy.backend.user.util.JwtUtil;
import com.ssafy.backend.user.util.RandomNickname;
import jakarta.transaction.Transactional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    private final UserProfileRepository userProfileRepository;
    private final UserInfoRepository userInfoRepository;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    public UserServiceImpl(UserProfileRepository userProfileRepository,
                           UserInfoRepository userInfoRepository,
                           JwtUtil jwtUtil,
                           RedisTemplate<String,Object> redisTemplate) {
        this.userProfileRepository = userProfileRepository;
        this.userInfoRepository = userInfoRepository;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }


    private Optional<UserProfile> findUserInfoByUserCode(String userCode){
        return userProfileRepository.findByUserCode(userCode);
    }

    private String getUserCode() {
        String testCode = null;
        int cnt = 0;
        do {
            cnt++;
            testCode = RandomNickname.generateRandomString();
//            System.out.println(testCode);
        } while (cnt<10 && !findUserInfoByUserCode(testCode).isEmpty());
        if (cnt==10) return null;
        return testCode;
    }

    @Override
    @Transactional
    public String signUp(String id, String password, String nickname) {
        String userCode = getUserCode();
        String token = null;
        if (userCode == null) throw new RuntimeException("유저코드 생성 실패");
        UserProfile userProfile = UserProfile.builder()
                .userNickname(nickname)
                .userCode(userCode)
                .build();

        userProfileRepository.save(userProfile);

//        System.out.println(userProfile.getId());
        UserInfo userInfo = UserInfo.builder()
//                .id(userProfile.getId()) 넣으면 안된다.
                .userId(id)
                .userPwd(password)
                .userProfile(userProfile)
                .build();

        Object result = userInfoRepository.save(userInfo);

        token = jwtUtil.generateToken(userProfile.getUserCode());
        redisTemplate.opsForValue().set("token:" + userCode, token, 3600, TimeUnit.SECONDS);


        return token;
    }
    @Override
    public String generateToken(String userCode){

    }
}
