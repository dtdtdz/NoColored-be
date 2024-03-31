package com.ssafy.backend.user.service;

import com.ssafy.backend.collection.document.UserCollection;
import com.ssafy.backend.collection.repository.UserCollectionRepository;
import com.ssafy.backend.rank.document.RankMongo;
import com.ssafy.backend.rank.repository.RankRepository;
import com.ssafy.backend.rank.util.RankUtil;
import com.ssafy.backend.user.entity.UserAchievements;
import com.ssafy.backend.user.repository.UserAchievementsRepository;
import com.ssafy.backend.user.repository.UserInfoRepository;
import com.ssafy.backend.user.dto.UserProfileDto;
import com.ssafy.backend.user.dto.UserSignDto;
import com.ssafy.backend.user.entity.UserInfo;
import com.ssafy.backend.user.entity.UserProfile;
import com.ssafy.backend.user.repository.UserProfileRepository;
import com.ssafy.backend.user.util.JwtUtil;
import com.ssafy.backend.user.util.RandomNickname;
import com.ssafy.backend.websocket.util.SessionCollection;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;


@Service
public class UserServiceImpl implements UserService {

    public static final int defaultRating = 2000;
    private final UserProfileRepository userProfileRepository;
    private final UserInfoRepository userInfoRepository;
    private final JwtUtil jwtUtil;
    private final ScheduledExecutorService authScheduledExecutorService;
    private final SessionCollection sessionCollection;
    private final RedisTemplate<String,Object> redisTemplate;
    private final UserCollectionRepository userCollectionRepository;
    private final RankRepository rankRepository;
    private final RankUtil rankUtil;
    private final UserAchievementsRepository userAchievementsRepository;
    public UserServiceImpl(UserProfileRepository userProfileRepository,
                           UserInfoRepository userInfoRepository,
                           JwtUtil jwtUtil,
                           @Qualifier("authScheduledExecutorService")ScheduledExecutorService authScheduledExecutorService,
                           SessionCollection sessionCollection, RedisTemplate<String, Object> redisTemplate, UserCollectionRepository userCollectionRepository, RankRepository rankRepository, RankUtil rankUtil, UserAchievementsRepository userAchievementsRepository
    ) {
        this.userProfileRepository = userProfileRepository;
        this.userInfoRepository = userInfoRepository;
        this.jwtUtil = jwtUtil;
        this.authScheduledExecutorService = authScheduledExecutorService;
        this.sessionCollection = sessionCollection;
        this.redisTemplate = redisTemplate;
        this.userCollectionRepository = userCollectionRepository;
        this.rankRepository = rankRepository;
        this.rankUtil = rankUtil;
        this.userAchievementsRepository = userAchievementsRepository;
    }

    private String getUserCode() {
        String testCode = null;
        int cnt = 0;
        do {
            cnt++;
            testCode = RandomNickname.generateRandomString();
//            System.out.println(testCode);
        } while (cnt<10 && userProfileRepository.existsByUserCode(testCode));
        if (cnt==10) return null;
        return testCode;
    }

    @Override
    public UserProfileDto getUserProfileDto(String token) {
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        //계산해야됨 어디서 해야됨?
        return userAccessInfo.getUserProfileDto();
    }

    @Override
    @Transactional
    public UserProfile guestSignUp(){
        try {
            String userCode = getUserCode();
            if (userCode == null) throw new RuntimeException("유저코드 생성 실패");
            UserProfile userProfile = UserProfile.builder()
                    .userNickname(RandomNickname.makeNickname())
                    .userCode(userCode)
                    .userExp(0L)
                    .userRating(defaultRating)
                    .userSkin("https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-basicblue.png")
                    .userLabel("손님")
                    .isGuest(true)
                    .build();
            userProfileRepository.save(userProfile);

            // userachievements
            UserAchievements userAchievements=UserAchievements.builder()
                    .userCode(userCode)
                    .lastLoginDate(LocalDateTime.now())
                    .consecutiveLoginDays(1)
                    .cumulativeLoginDays(1)
                    .isConsecutiveLogin(true)
                    .cumulativePlayCount(0)
                    .cumulativeWinCount(0)
                    .cumulativeLoseCount(0)
                    .playtime(0L)
                    .userProfile(userProfile)
                    .build();
            userAchievementsRepository.save(userAchievements);


            // usercollection 생성
            UserCollection userCollection=UserCollection.builder()
                    .userCode(userCode)
                    .skinIds(new ArrayList<>(Arrays.asList(11,15,19,21,23,27,29)))
                    .labelIds(new ArrayList<>(Arrays.asList(1,10,71)))
                    .achievementIds(new ArrayList<>())
                    .build();
            userCollectionRepository.save(userCollection);

            // mongodb에 넣을 rank정보
            RankMongo rankMongo=RankMongo.builder()
                    .userCode(userCode)
                    .rating(defaultRating)
                    .build();
            rankRepository.save(rankMongo);



            return userProfile;
        } catch (Exception e){
            throw new RuntimeException("게스트 생성 실패.");
        }
    }
    @Override
    @Transactional
    public String guestConvert(String token, UserSignDto userSignDto) {
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        UserProfile userProfile = userAccessInfo.getUserProfile();
        //userProfile 을 managed 상태로 만들어준다.
        userProfile = userProfileRepository.findById(userProfile.getId()).orElse(null);
        if (userProfile==null) return null;
        if (userInfoRepository.existsByUserId(userSignDto.getId())){
           throw new RuntimeException("이미 있는 id 입니다..");
        };

        userProfile.setGuest(false);
        userProfile.setUserNickname(userSignDto.getNickname());
        userProfile.setUserLabel("파릇파릇 새싹");

        userProfileRepository.save(userProfile);
        UserProfileDto userProfileDto=userAccessInfo.getUserProfileDto();
        userProfileDto.setGuest(false);
        userProfileDto.setNickname(userSignDto.getNickname());
        userProfileDto.setLabel("파릇파릇 새싹");

        doUserProfileDto(userProfileDto);
        // userAccessInfo에 반영
        userAccessInfo.setUserProfileDto(userProfileDto);

        // 파릇파릇 새싹 칭호 얻었다고 처리
        UserProfile tempUserProfile = userProfile;
        Optional<UserCollection> userCollectionOptional=userCollectionRepository.findById(tempUserProfile.getUserCode());
        UserCollection userCollection=userCollectionOptional.orElseThrow(() ->
                new NoSuchElementException("해당 사용자의 UserCollection이 존재하지 않습니다: " + tempUserProfile.getUserCode()));
        userCollection.getLabelIds().add(72);
        userCollection.getLabelIds().add(64); // 게스트 로그인 회원 전환
        userCollection.getSkinIds().add(28);
        userCollectionRepository.save(userCollection);
        
        UserInfo userInfo = UserInfo.builder()
//                .id(userProfile.getId()) 넣으면 안된다.
                .userId(userSignDto.getId())
                .userPwd(userSignDto.getPassword())
                .userProfile(userProfile)
                .isDeleted(false)
                .build();
        userInfoRepository.save(userInfo);
        return token;
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
                    .userRating(defaultRating)
                    .userSkin("https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-basicblue.png")
                    .userLabel("파릇파릇 새싹")
                    .isGuest(false)
                    .build();
            userProfileRepository.save(userProfile);

            // userachievements
            UserAchievements userAchievements=UserAchievements.builder()
                    .userCode(userCode)
                    .lastLoginDate(LocalDateTime.now())
                    .consecutiveLoginDays(1)
                    .cumulativeLoginDays(1)
                    .isConsecutiveLogin(true)
                    .cumulativePlayCount(0)
                    .cumulativeWinCount(0)
                    .cumulativeLoseCount(0)
                    .playtime(0L)
                    .userProfile(userProfile)
                    .build();
            userAchievementsRepository.save(userAchievements);

            // usercollection 생성
            UserCollection userCollection=UserCollection.builder()
                    .userCode(userCode)
                    .skinIds(new ArrayList<>(Arrays.asList(11,15,19,21,23,27,28,29))) // 게스트->회원전환할때 보상도 다 포함
                    .labelIds(new ArrayList<>(Arrays.asList(1,10,64,71,72)))
                    .achievementIds(new ArrayList<>())
                    .build();
            userCollectionRepository.save(userCollection);

            // mongodb에 넣을 rank정보
            RankMongo rankMongo=RankMongo.builder()
                    .userCode(userCode)
                    .rating(userProfile.getUserRating())
                    .build();
            rankRepository.save(rankMongo);

            // redis에 넣기
            // rankUtil.createUserRankRedis(userProfileDto);


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
        UserAccessInfo userAccessInfo = new UserAccessInfo(userProfile);
        sessionCollection.userIdMap.put(userProfile.getId(), userAccessInfo);

//        Integer rating= (Integer) redisTemplate.opsForValue().get(userProfile.getUserRating());
//        int userCount=redisTemplate.keys("*").size();

        userAccessInfo.setUserProfileDto(new UserProfileDto(userProfile));
        System.out.println(token);
//        authScheduledExecutorService.schedule(()->{
//            if (!sessionCollection.userIdMap.containsKey(userProfile.getId())){
//                jwtUtil.deleteTokenRedis(token);
//            } else if (sessionCollection.userIdMap.get(userProfile.getId()).getSession()==null){
//                jwtUtil.deleteTokenRedis(token);
//                sessionCollection.userIdMap.remove(userProfile.getId());
//            }
//        },10, TimeUnit.SECONDS);

        return token;
    }

    @Override
    public String login(String id, String password) {
        UserProfile userProfile = userInfoRepository.findByUser(id, password);
        if (userProfile==null) return null;

        UserCollection userCollection=userCollectionRepository.findByUserCode(userProfile.getUserCode());

        // 누적, 연속접속 확인
        UserAchievements userAchievements=userAchievementsRepository.findByUserCode(userProfile.getUserCode());
        LocalDateTime today=LocalDateTime.now();
        LocalDateTime lastLoginDate=userAchievements.getLastLoginDate();
        // 누적접속 +1
        userAchievements.setCumulativeLoginDays(userAchievements.getCumulativeLoginDays()+1);
        
        // 연속접속
        if(lastLoginDate.equals(today.minusDays(1))){
            userAchievements.setConsecutiveLoginDays(userAchievements.getConsecutiveLoginDays()+1);
            userAchievements.setConsecutiveLogin(true);
        }else if(!lastLoginDate.equals(today)){
            userAchievements.setConsecutiveLogin(false);
            userAchievements.setConsecutiveLoginDays(1);
            userCollection.getLabelIds().add(68);
        }
        userAchievements.setLastLoginDate(today);

        // 누적 접속
        if(userAchievements.getCumulativeLoginDays()==2){
            userCollection.getLabelIds().add(2);
            userCollection.getSkinIds().add(30);
        }else if(userAchievements.getCumulativeLoginDays()==3){
            userCollection.getLabelIds().add(3);
            userCollection.getSkinIds().add(25);
        }else if(userAchievements.getCumulativeLoginDays()==4){
            userCollection.getLabelIds().add(4);
            userCollection.getSkinIds().add(26);
        }else if(userAchievements.getCumulativeLoginDays()==5){
            userCollection.getLabelIds().add(5);
        }else if(userAchievements.getCumulativeLoginDays()==7){
            userCollection.getLabelIds().add(6);
        }else if(userAchievements.getCumulativeLoginDays()==10){
            userCollection.getLabelIds().add(7);
        }else if(userAchievements.getCumulativeLoginDays()==15){
            userCollection.getLabelIds().add(8);
        }else if(userAchievements.getCumulativeLoginDays()==30){
            userCollection.getLabelIds().add(9);
        }
        // 연속 접속
        if(userAchievements.getConsecutiveLoginDays()==2){
            userCollection.getLabelIds().add(11);
            userCollection.getSkinIds().add(22);
        }else if(userAchievements.getConsecutiveLoginDays()==3){
            userCollection.getLabelIds().add(12);
            userCollection.getSkinIds().add(13);
        }else if(userAchievements.getConsecutiveLoginDays()==4){
            userCollection.getLabelIds().add(13);
            userCollection.getSkinIds().add(14);
        }else if(userAchievements.getConsecutiveLoginDays()==5){
            userCollection.getLabelIds().add(14);
        }else if(userAchievements.getConsecutiveLoginDays()==6){
            userCollection.getLabelIds().add(15);
        }else if(userAchievements.getConsecutiveLoginDays()==7){
            userCollection.getLabelIds().add(16);
        }else if(userAchievements.getConsecutiveLoginDays()==8){
            userCollection.getLabelIds().add(17);
        }else if(userAchievements.getConsecutiveLoginDays()==9){
            userCollection.getLabelIds().add(18);
        }else if(userAchievements.getConsecutiveLoginDays()==10){
            userCollection.getLabelIds().add(19);
        }else if(userAchievements.getConsecutiveLoginDays()==12){
            userCollection.getLabelIds().add(20);
        }else if(userAchievements.getConsecutiveLoginDays()==14){
            userCollection.getLabelIds().add(21);
        }else if(userAchievements.getConsecutiveLoginDays()==16){
            userCollection.getLabelIds().add(22);
        }else if(userAchievements.getConsecutiveLoginDays()==18){
            userCollection.getLabelIds().add(23);
        }else if(userAchievements.getConsecutiveLoginDays()==20){
            userCollection.getLabelIds().add(24);
        }

        userAchievementsRepository.save(userAchievements);
        userCollectionRepository.save(userCollection);

        return generateToken(userProfile);
    }

    @Override
    public void updatePassword(String token, String pwd, String prePwd) {
        UserAccessInfo user = jwtUtil.getUserAccessInfoRedis(token);
        if (!prePwd.equals(userInfoRepository.findUserPwdById(user.getUserProfile().getId())))
            throw new RuntimeException("Wrong password");
        if (pwd.length()<6 || pwd.length()>20)
            throw new RuntimeException("Password does not meet the length requirements (6-20 characters).");
        userInfoRepository.updatePassword(user.getUserProfile().getId(), pwd);
    }

    @Override
    public void updateNickname(String token, String nickname) {
        UserAccessInfo user = jwtUtil.getUserAccessInfoRedis(token);
        userProfileRepository.updateNickname(user.getUserProfile().getId(), nickname);
        user.getUserProfile().setUserNickname(nickname);
        user.getUserProfileDto().setNickname(nickname);
    }

    @Override
    public boolean confirmUser(String token, String password) {
        UserAccessInfo user = jwtUtil.getUserAccessInfoRedis(token);

        return password.equals(userInfoRepository.findUserPwdById(user.getUserProfile().getId()));
    }

    @Override
    public void deleteUser(String token) {
        UserAccessInfo user = jwtUtil.getUserAccessInfoRedis(token);
        userInfoRepository.deleteUser(user.getUserProfile().getId());
        //token 제거
        jwtUtil.deleteTokenRedis(token);
    }

    @Override
    public boolean existsUserId(String userId) {
        return userInfoRepository.existsByUserId(userId);
    }

    @Override
    public UserProfileDto findUserInfo(String userCode) {
        UserProfile userProfile = userProfileRepository.findByUserCode(userCode).orElse(null);
        if (userProfile==null) return null;
        return new UserProfileDto(userProfile);
    }

    @Override
    public void logout(String token) {
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        jwtUtil.deleteTokenRedis(token);
        
    }

    private void calcLevelExp(UserProfileDto userProfileDto){
        long currentExp=userProfileDto.getExp();
        int level=0;
        long reqExp=50;
        while(currentExp>=reqExp){
            currentExp-=reqExp;
            level++;
            if(level<=10){
                reqExp=500;
            }else if(level<=30) {
                reqExp=1000;
            }else if(level<=50) {
                reqExp=1500;
            }else if(level<=75) {
                reqExp=2000;
            }else{
                reqExp=3000;
            }
        }
        userProfileDto.setLevel(level);
        userProfileDto.setExp(currentExp);
        userProfileDto.setExpRequire(reqExp);
    }

    public void doUserProfileDto(UserProfileDto userProfileDto){
        calcLevelExp(userProfileDto); // exp, expRequire, level 계산
        if(!userProfileDto.isGuest()){
            rankUtil.createUserRankRedis(userProfileDto); // redis에 넣기
            rankUtil.getMyRank(userProfileDto); // 순위, 티어 계산
        }else{
            rankUtil.getMyRankGuest(userProfileDto);
        }

    }

}
