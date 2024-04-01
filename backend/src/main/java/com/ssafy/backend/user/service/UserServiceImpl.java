package com.ssafy.backend.user.service;

import com.ssafy.backend.collection.document.UserCollection;
import com.ssafy.backend.collection.repository.UserCollectionRepository;
import com.ssafy.backend.play.domain.RoomInfo;
import com.ssafy.backend.play.dto.RoomDto;
import com.ssafy.backend.play.dto.UserRoomDto;
import com.ssafy.backend.play.service.FriendlyService;
import com.ssafy.backend.play.service.FriendlyServiceImpl;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
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
    private final FriendlyService friendlyService;
    public UserServiceImpl(UserProfileRepository userProfileRepository,
                           UserInfoRepository userInfoRepository,
                           JwtUtil jwtUtil,
                           @Qualifier("authScheduledExecutorService")ScheduledExecutorService authScheduledExecutorService,
                           SessionCollection sessionCollection, RedisTemplate<String, Object> redisTemplate, UserCollectionRepository userCollectionRepository, RankRepository rankRepository, RankUtil rankUtil, UserAchievementsRepository userAchievementsRepository,
                           FriendlyService friendlyService

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
        this.friendlyService = friendlyService;
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
        if (userAccessInfo==null) return null;
        doUserProfileDto(userAccessInfo);
        return userAccessInfo.getUserProfileDto();
    }

    @Override
    @Transactional
    public UserAccessInfo guestSignUp(){
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
            userProfile = userProfileRepository.save(userProfile);
            if (userProfile.getId() == null) throw new RuntimeException("게스트 생성 실패");
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
                    .step(0)
                    .stepped(0)
                    .lightUPallCount(0)
                    .stopNPCCount(0)
                    .randomBoxCount(0)
                    .rebelCount(0)
                    .stopPlayerCount(0)
                    .itemCount(0)
                    .userProfile(userProfile)
                    .build();
            userAchievements = userAchievementsRepository.save(userAchievements);


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
            UserAccessInfo userAccessInfo = new UserAccessInfo(userProfile);
            userAccessInfo.setUserAchievements(userAchievements);
            return userAccessInfo;
        } catch (Exception e) {
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

        doUserProfileDto(userAccessInfo);
        // userAccessInfo에 반영
        userAccessInfo.setUserProfileDto(userProfileDto);

        // 파릇파릇 새싹 칭호 얻었다고 처리
        UserProfile tempUserProfile = userProfile;
        Optional<UserCollection> userCollectionOptional=userCollectionRepository.findById(tempUserProfile.getUserCode());
        UserCollection userCollection=userCollectionOptional.orElseThrow(() ->
                new NoSuchElementException("해당 사용자의 UserCollection이 존재하지 않습니다: " + tempUserProfile.getUserCode()));
        userCollection.getLabelIds().add(72);
        userCollection.getLabelIds().add(64); // 게스트 로그인 회원 전환
        userCollection.getSkinIds().add(12);
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
    public UserAccessInfo signUp(String id, String password, String nickname) {
        if (userInfoRepository.existsByUserId(id)) throw new RuntimeException("유저 id 중복");

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
        userProfile = userProfileRepository.save(userProfile);
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
                .step(0)
                .stepped(0)
                .lightUPallCount(0)
                .stopNPCCount(0)
                .randomBoxCount(0)
                .rebelCount(0)
                .stopPlayerCount(0)
                .itemCount(0)
                .userProfile(userProfile)
                .build();
        userAchievements = userAchievementsRepository.save(userAchievements);

        // usercollection 생성
        UserCollection userCollection=UserCollection.builder()
                .userCode(userCode)
                .skinIds(new ArrayList<>(Arrays.asList(11,12,15,19,21,23,27,29))) // 게스트->회원전환할때 보상도 다 포함
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


        UserAccessInfo userAccessInfo = new UserAccessInfo(userProfile);
        userAccessInfo.setUserAchievements(userAchievements);

        return userAccessInfo;
    }
    @Override
    public String generateToken(UserAccessInfo userAccessInfo){
        UserProfile userProfile = userAccessInfo.getUserProfile();
        String token = jwtUtil.generateToken(userProfile.getUserCode());
        jwtUtil.setTokenRedis(token, userProfile.getId());
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
        UserAccessInfo userAccessInfo = new UserAccessInfo(userProfile);
        userAccessInfo.setUserAchievements(userAchievements);
        return generateToken(userAccessInfo);
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
        UserAccessInfo dummyUser = new UserAccessInfo(userProfile);
        dummyUser.setUserProfileDto(new UserProfileDto(userProfile));
        doUserProfileDto(dummyUser);
        return dummyUser.getUserProfileDto();
    }





    private void doUserProfileDto(UserAccessInfo userAccessInfo){
        UserProfileDto userProfileDto = userAccessInfo.getUserProfileDto();
        userProfileDto.calcLevelExp(userAccessInfo.getUserProfile().getUserExp()); // exp, expRequire, level 계산
        if(!userProfileDto.isGuest()){
            rankUtil.createUserRankRedis(userProfileDto); // redis에 넣기
            rankUtil.getMyRank(userProfileDto); // 순위, 티어 계산
        }else{
            rankUtil.getMyRankGuest(userProfileDto);
        }

    }

//    @Scheduled(cron = "0 0/10 * * * *")
    public void scheduledUserLogout() {
        for (UserAccessInfo userAccessInfo: sessionCollection.userIdMap.values()){
            synchronized (userAccessInfo){
                if (userAccessInfo.isExpire()){
                    logout(userAccessInfo);
                }
            }

        }
    }

    @Override
    public void activeLogout(String token) {
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        jwtUtil.deleteTokenRedis(token);

    }

    @Override
    public boolean isTokenValid(String token) {
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        return (userAccessInfo!=null);
    }

    public void logoutRoomExit(UserAccessInfo userAccessInfo){

        RoomInfo roomInfo = userAccessInfo.getRoomInfo();
        UserAccessInfo[] userAccessInfos=roomInfo.getUserAccessInfos();
        // 내 위치 찾기
        for(int i=0;i<4;i++) {
            // 찾으면 해당하는 userRoomDto의 상태 변경
            if (userAccessInfos[i] != null && userAccessInfos[i].getUserProfile().getUserCode().equals(userAccessInfo.getUserProfile().getUserCode())) {
                RoomDto roomDto = roomInfo.getRoomDto();
                UserRoomDto[] players = roomDto.getPlayers();

                // 방장이면
                if (i == roomDto.getMasterIndex()) {
                    // 방에 있는 유저 수
                    int userNumber = (int) Arrays.stream(userAccessInfos).filter(Objects::nonNull).count();

                    // 방장 혼자 있다면
                    if(userNumber==1){
                        // 자신 정보 바꾸고 맵에서 방 삭제
                        userAccessInfo.clearPosition();
                        friendlyService.getRoomInfoMap().remove(roomInfo.getRoomCodeInt());
                        friendlyService.getUuidRoomInfoMap().remove(roomInfo.getRoomDto().getRoomId());
                    }else{
                        // 방장을 넘겨줄 사람 찾기
                        int startIndex=0;

                        // 자신 정보 바꾸기
                        players[i].setEmptyUser();
                        userAccessInfos[i]=null;
                        userAccessInfo.clearPosition();

                        for (int j=i+1; j<userAccessInfos.length; j++){
                            userAccessInfos[j-1] = userAccessInfos[j];
                            if (userAccessInfos[j-1]==null){
                                players[j-1].setEmptyUser();
                                break;
                            } else {
                                players[j-1].setUser(userAccessInfos[j-1].getUserProfileDto());
                            }
                            if (roomDto.getMasterIndex()==j) roomDto.setMasterIndex(j-1);
                        }
                        userAccessInfos[userAccessInfos.length-1] = null;
                        players[userAccessInfos.length-1].setEmptyUser();

                        // 최대 3번 탐색
                        for(int j=0;j<3;j++){
                            // 범위 벗어나면
                            // 넘겨줄 사람 찾으면 넘기기
                            if(userAccessInfos[startIndex]!=null){
                                roomDto.setMasterIndex(startIndex);
                                // 변경했다고 세션 뿌리기
                                friendlyService.sendRoomDto(roomInfo);
                            }else{
                                startIndex++;
                            }
                        }
                    }
                }else{
                    // 방장 아니면
                    // 자신 위치의 정보 초기화
                    players[i].setEmptyUser();
                    // roomInfo 반영
                    userAccessInfos[i]=null;
                    userAccessInfo.clearPosition();

                    for (int j=i+1; j<userAccessInfos.length; j++){
                        userAccessInfos[j-1] = userAccessInfos[j];
                        if (userAccessInfos[j-1]==null){
                            players[j-1].setEmptyUser();
                            break;
                        } else {
                            players[j-1].setUser(userAccessInfos[j-1].getUserProfileDto());
                        }
                        if (roomDto.getMasterIndex()==j) roomDto.setMasterIndex(j-1);
                    }
                    userAccessInfos[userAccessInfos.length-1] = null;
                    players[userAccessInfos.length-1].setEmptyUser();

                    friendlyService.sendRoomDto(roomInfo);
                }
            }
        }
    }
    public void logout(UserAccessInfo userAccessInfo){
        if (userAccessInfo.getRoomInfo()!=null){
            logoutRoomExit(userAccessInfo);
        }
    }
}
