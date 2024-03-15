package com.ssafy.backend.user.service;

import com.ssafy.backend.user.dao.UserInfoRepository;
import com.ssafy.backend.user.entity.UserInfo;
import com.ssafy.backend.user.entity.UserProfile;
import com.ssafy.backend.user.dao.UserProfileRepository;
import com.ssafy.backend.user.util.JwtUtil;
import com.ssafy.backend.user.util.RandomNickname;
import com.ssafy.backend.websocket.util.SessionCollection;
import com.ssafy.backend.game.domain.UserAccessInfo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    private final UserProfileRepository userProfileRepository;
    private final UserInfoRepository userInfoRepository;
    private final JwtUtil jwtUtil;
    private final ScheduledExecutorService authScheduledExecutorService;
    private final SessionCollection sessionCollection;
    public UserServiceImpl(UserProfileRepository userProfileRepository,
                           UserInfoRepository userInfoRepository,
                           JwtUtil jwtUtil,
                           @Qualifier("authScheduledExecutorService")ScheduledExecutorService authScheduledExecutorService,
                           SessionCollection sessionCollection
                           ) {
        this.userProfileRepository = userProfileRepository;
        this.userInfoRepository = userInfoRepository;
        this.jwtUtil = jwtUtil;
        this.authScheduledExecutorService = authScheduledExecutorService;
        this.sessionCollection = sessionCollection;
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
        } while (cnt<10 && findUserInfoByUserCode(testCode).isPresent());
        if (cnt==10) return null;
        return testCode;
    }

    @Override
    public UserProfile guestSignUp(){
        try {
            String userCode = getUserCode();
            if (userCode == null) throw new RuntimeException("유저코드 생성 실패");
            UserProfile userProfile = UserProfile.builder()
                    .userNickname(RandomNickname.makeNickname())
                    .userCode(userCode)
                    .build();
            userProfileRepository.save(userProfile);
            return userProfile;
        } catch (Exception e){
            throw new RuntimeException("게스트 생성 실패.");
        }
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
                    .userExp(0L)
                    .userRating(0)
                    .userSkin("")
                    .userTitle("")
                    .build();

            userProfileRepository.save(userProfile);

//        System.out.println(userProfile.getId());
            UserInfo userInfo = UserInfo.builder()
//                .id(userProfile.getId()) 넣으면 안된다.
                    .userId(id)
                    .userPwd(password)
                    .userProfile(userProfile)
                    .isDeleted(false)
                    .build();

            userInfoRepository.save(userInfo);
            return userProfile;

        } catch (Exception e){
            throw new RuntimeException("이미 있는 id입니다.");
        }
    }
    @Override
    public String generateToken(UserProfile userProfile){
        String token = jwtUtil.generateToken(userProfile.getUserCode());
        jwtUtil.setTokenRedis(token, userProfile.getId());
        sessionCollection.userIdMap.put(userProfile.getId(), new UserAccessInfo(userProfile));
        authScheduledExecutorService.schedule(()->{
            if (!sessionCollection.userIdMap.containsKey(userProfile.getId())){
                jwtUtil.deleteTokenRedis(token);
            } else if (sessionCollection.userIdMap.get(userProfile.getId()).getSession()==null){
                jwtUtil.deleteTokenRedis(token);
                sessionCollection.userIdMap.remove(userProfile.getId());
            }
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
