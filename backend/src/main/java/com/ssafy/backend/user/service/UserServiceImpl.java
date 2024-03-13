package com.ssafy.backend.user.service;

import com.ssafy.backend.user.dao.UserInfoRepository;
import com.ssafy.backend.user.entity.UserInfo;
import com.ssafy.backend.user.entity.UserProfile;
import com.ssafy.backend.user.dao.UserProfileRepository;
import com.ssafy.backend.user.util.JwtUtil;
import com.ssafy.backend.user.util.RandomNickname;
import com.ssafy.backend.websocket.dao.SessionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    private final UserProfileRepository userProfileRepository;
    private final UserInfoRepository userInfoRepository;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    private final ScheduledExecutorService authScheduledExecutorService;
    public UserServiceImpl(UserProfileRepository userProfileRepository,
                           UserInfoRepository userInfoRepository,
                           JwtUtil jwtUtil,
                           RedisTemplate<String,Object> redisTemplate,
                           @Qualifier("authScheduledExecutorService")ScheduledExecutorService authScheduledExecutorService
                           ) {
        this.userProfileRepository = userProfileRepository;
        this.userInfoRepository = userInfoRepository;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
        this.authScheduledExecutorService = authScheduledExecutorService;
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
    public UserProfile signUp(String id, String password, String nickname) {
        try {
            String userCode = getUserCode();
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

            userInfoRepository.save(userInfo);
            return userProfile;

        } catch (Exception e){
            throw new RuntimeException("이미 있는 id");
        }
    }
    @Override
    public String generateToken(UserProfile userProfile){
        String token = jwtUtil.generateToken(userProfile.getUserCode());
        redisTemplate.opsForValue().set("token:" + token, userProfile.getId(), 3600*8, TimeUnit.SECONDS);//8시간 살아있음
        authScheduledExecutorService.schedule(()->{
            if (!SessionRepository.userTokenMap.containsKey(token)){
                redisTemplate.delete("token:" + token);
            }
//            redisTemplate.opsForValue().remo
        },10, TimeUnit.SECONDS);
        return token;
    }

    @Override
    public String login(String id, String password) {
        UserProfile userProfile = userInfoRepository.findByUser(id, password);
        if (userProfile==null) return null;

        return generateToken(userProfile);
    }
}
