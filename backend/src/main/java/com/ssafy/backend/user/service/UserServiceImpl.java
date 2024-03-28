package com.ssafy.backend.user.service;

import com.ssafy.backend.collection.dao.UserCollection;
import com.ssafy.backend.collection.repository.UserCollectionRepository;
import com.ssafy.backend.rank.dao.RankMongo;
import com.ssafy.backend.rank.repository.RankRepository;
import com.ssafy.backend.rank.util.RankUtil;
import com.ssafy.backend.user.dao.UserInfoRepository;
import com.ssafy.backend.user.dto.UserProfileDto;
import com.ssafy.backend.user.dto.UserSignDto;
import com.ssafy.backend.user.entity.UserInfo;
import com.ssafy.backend.user.entity.UserProfile;
import com.ssafy.backend.user.dao.UserProfileRepository;
import com.ssafy.backend.user.util.JwtUtil;
import com.ssafy.backend.user.util.RandomNickname;
import com.ssafy.backend.websocket.util.SessionCollection;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

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
    public UserServiceImpl(UserProfileRepository userProfileRepository,
                           UserInfoRepository userInfoRepository,
                           JwtUtil jwtUtil,
                           @Qualifier("authScheduledExecutorService")ScheduledExecutorService authScheduledExecutorService,
                           SessionCollection sessionCollection, RedisTemplate<String, Object> redisTemplate, UserCollectionRepository userCollectionRepository, RankRepository rankRepository, RankUtil rankUtil
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

            // usercollection 생성
            UserCollection userCollection=UserCollection.builder()
                    .userCode(userCode)
                    .skinIds(new ArrayList<>(Arrays.asList(1,2,3,4,27)))
                    .labelIds(new ArrayList<>(Arrays.asList(92)))
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
        userProfile.setGuest(false);
        userProfile.setUserNickname(userSignDto.getNickname());
        userProfile.setUserLabel("파릇파릇 새싹");
        userProfileRepository.save(userProfile);
        UserProfileDto userProfileDto=new UserProfileDto(userProfile);
        calcLevelExp(userProfileDto);
        userAccessInfo.setUserProfileDto(userProfileDto);
//        userAccessInfo.getUserProfileDto().setTier(tierCalculation());
        // 순위, 티어 계산해주기
        

        // 파릇파릇 새싹 칭호 얻었다고 처리
        UserProfile tempUserProfile = userProfile;
        Optional<UserCollection> userCollectionOptional=userCollectionRepository.findById(tempUserProfile.getUserCode());
        UserCollection userCollection=userCollectionOptional.orElseThrow(() ->
                new NoSuchElementException("해당 사용자의 UserCollection이 존재하지 않습니다: " + tempUserProfile.getUserCode()));
        userCollection.getLabelIds().add(93);
        userCollectionRepository.save(userCollection);

//        // rankMongo에 기본 레이팅 값 주기
//        Optional<RankMongo> rankMongoOptional=rankRepository.findById(userProfile.getUserCode());
//        RankMongo rankMongo=rankMongoOptional.get();
//        rankMongo.setRating(defaultRating);
//        rankRepository.save(rankMongo);
        
        UserInfo userInfo = UserInfo.builder()
//                .id(userProfile.getId()) 넣으면 안된다.
                .userId(userSignDto.getId())
                .userPwd(userSignDto.getPassword())
                .userProfile(userProfile)
                .isDeleted(false)
                .build();
        userInfoRepository.save(userInfo);

        // redis에 넣기
        rankUtil.createUserRankRedis(userProfile);

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

            // usercollection 생성
            UserCollection userCollection=UserCollection.builder()
                    .userCode(userCode)
                    .skinIds(new ArrayList<>(Arrays.asList(1,2,3,4,27)))
                    .labelIds(new ArrayList<>(Arrays.asList(93)))
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
            rankUtil.createUserRankRedis(userProfile);


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
        //계산해야됨 티어, 랭킹
        //nocolored=맨처음
        //bronze=1판함
        //silver=3000까지
        //gold=3500까지
        //platinum=4200까지
        //diamond=5000까지
        //colored=상위10등
        //rgb=상위5등
        //origin=1등

        // zset에 넣는거를 게임 끝날때마다 하자
        
        
        // 레디스 zset으로 되어있음?->이거 어캐알음???아 넣을떄(게임끝날때) zset으로만 박자
        // 가져오는 rating 값이 없을 수 있나?
        // 기본
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

}
