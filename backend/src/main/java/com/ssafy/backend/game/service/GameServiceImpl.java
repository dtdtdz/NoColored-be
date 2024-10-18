package com.ssafy.backend.game.service;

import com.ssafy.backend.collection.document.UserCollection;
import com.ssafy.backend.collection.repository.UserCollectionRepository;
import com.ssafy.backend.game.document.UserPlayInfo;
import com.ssafy.backend.game.domain.ResultInfo;
import com.ssafy.backend.game.dto.ResultDto;
import com.ssafy.backend.game.dto.RewardDto;
import com.ssafy.backend.game.dto.TierDto;
import com.ssafy.backend.game.dto.UserResultDto;
import com.ssafy.backend.game.util.InGameCollection;
import com.ssafy.backend.game.util.InGameLogic;
import com.ssafy.backend.rank.util.RankUtil;
import com.ssafy.backend.user.dto.UserProfileDto;
import com.ssafy.backend.user.entity.UserAchievements;
import com.ssafy.backend.user.entity.UserProfile;
import com.ssafy.backend.user.repository.UserAchievementsRepository;
import com.ssafy.backend.user.repository.UserProfileRepository;
import com.ssafy.backend.user.util.JwtUtil;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import com.ssafy.backend.websocket.util.SessionCollection;
import com.ssafy.backend.game.domain.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import static com.ssafy.backend.game.domain.GameInfo.GAME_TIME;
import static com.ssafy.backend.rank.util.RankUtil.tierCalculation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
@Service
public class GameServiceImpl implements GameService {

    private final ScheduledExecutorService scheduledExecutorService;
    private final ScheduledExecutorService saveScheduledExecutorService;
    private final UserProfileRepository userProfileRepository;
//    private final ScheduledExecutorService
    private final SessionCollection sessionCollection;
    private final InGameCollection inGameCollection;
    private final InGameLogic inGameLogic;
    private final JwtUtil jwtUtil;
    private ScheduledFuture<?> future;
    private final UserAchievementsRepository userAchievementsRepository;
    private final UserCollectionRepository userCollectionRepository;
    private final RankUtil rankUtil;
    GameServiceImpl(@Qualifier("scheduledExecutorService")ScheduledExecutorService scheduledExecutorService,
                    @Qualifier("authScheduledExecutorService")ScheduledExecutorService saveScheduledExecutorService,
                    UserProfileRepository userProfileRepository,
                    SessionCollection sessionRepository,
                    InGameCollection inGameRepository,
                    InGameLogic inGameLogic,
                    JwtUtil jwtUtil, UserAchievementsRepository userAchievementsRepository, UserCollectionRepository userCollectionRepository, RankUtil rankUtil){
        this.scheduledExecutorService = scheduledExecutorService;
        this.saveScheduledExecutorService = saveScheduledExecutorService;
        this.userProfileRepository = userProfileRepository;
        this.sessionCollection = sessionRepository;
        this.inGameCollection = inGameRepository;
        this.inGameLogic = inGameLogic;
        this.jwtUtil = jwtUtil;
        this.userAchievementsRepository = userAchievementsRepository;
        this.userCollectionRepository = userCollectionRepository;
        this.rankUtil = rankUtil;
    }

    @Override
    public synchronized GameRoomDto ready(String token) {
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        GameInfo gameInfo = userAccessInfo.getGameInfo();
        if (gameInfo==null) return null;
        return gameInfo.getGameRoomDto();
    }

    // resultdto를 만들어줌 그래서 그거 가지고 유저 업적을 할 수 있음
    @Override
    public ResultDto getResult(String token) {
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        ResultDto resultDto = new ResultDto(userAccessInfo.getResultInfo());

        Map<String,Integer> tierList=new HashMap<>();
        tierList.put("nocolored",0);
        tierList.put("bronze",1);
        tierList.put("silver",2);
        tierList.put("gold",3);
        tierList.put("platinum",4);
        tierList.put("diamond",5);
        tierList.put("colored",6);
        tierList.put("rgb",7);
        tierList.put("origin",8);

        // 매칭이면
        if (userAccessInfo.getResultInfo().getGameInfo().getRoom()==null){
            // 게스트여도 티어 계산
            String oldTier = userAccessInfo.getUserProfileDto().getTier();
            String newTier = tierCalculation(userAccessInfo.getUserProfileDto().getRank(),
                    userAccessInfo.getUserProfile().getUserRating(), userAccessInfo.getUserProfile().getUserExp());
                    userAccessInfo.getUserProfileDto().setTier(newTier);
            TierDto tierDto = new TierDto();
            tierDto.setNewtier(newTier);
            tierDto.setOldtier(oldTier);
            boolean tierUpgrade = tierList.get(newTier) - tierList.get(oldTier) > 0;
            tierDto.setUpgrade(tierUpgrade);
            resultDto.getReward().setTier(tierDto);
            userAccessInfo.clearPosition();

        }else{
            // 친선전이면
            userAccessInfo.setRoomInfo(userAccessInfo.getResultInfo().getGameInfo().getRoom()); // 추가
            resultDto.getReward().setTier(new TierDto());
        }
        // 스킨 처리
        resultDto.getReward().setSkins(new ArrayList<>());


        return resultDto;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void scheduleTaskAfterStartup() {
        long initialDelay = 0; // 시작 지연 없음
        long period = 16_666; // 17ms
        
        future = scheduledExecutorService
        .scheduleAtFixedRate(this::gameLogic, initialDelay, period, TimeUnit.MICROSECONDS);
    }

    private void gameLogic(){
        try {
            inGameCollection.updateGameList();
            Iterator<GameInfo> gameInfoIterator = inGameCollection.getGameInfoIterator();
            while (gameInfoIterator.hasNext()){
                GameInfo gameInfo = gameInfoIterator.next();

                try {
                    switch (gameInfo.getGameCycle()){
                        case CREATE -> inGameLogic.create(gameInfo);
                        case READY -> inGameLogic.ready(gameInfo);
                        case PLAY -> inGameLogic.play(gameInfo);
                        case CLOSE -> gameClose(gameInfo);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                    throw e;
                }
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    private void dataSave(GameInfo gameInfo){

        saveScheduledExecutorService.schedule(()->{
            try {
                for (Map.Entry<UserAccessInfo, UserGameInfo> entry:gameInfo.getUsers().entrySet()){
                    UserProfile userProfile = userProfileRepository.findById(
                            entry.getKey().getUserProfile().getId()).orElse(null);
                    if (userProfile==null) throw new RuntimeException("Can't find user");

                    userProfile.setUserExp(entry.getKey().getUserProfile().getUserExp());
                    userProfile.setUserRating(entry.getKey().getUserProfile().getUserRating());

//                    System.out.println(userProfile.getUserCode()+" 의 exp는 "+userProfile.getUserExp());
//                    System.out.println(userProfile.getUserCode()+" 의 rating는 "+userProfile.getUserRating());

                    UserAccessInfo userAccessInfo=entry.getKey();
                    UserCollection userCollection=userCollectionRepository.findByUserCode(userProfile.getUserCode());
                    UserAchievements userAchievements=userAchievementsRepository.findByUserCode(userProfile.getUserCode());
//                    RewardDto reward=new RewardDto();
                    UserPlayInfo userPlayInfo=userAccessInfo.getResultInfo().getGameInfo().getUsers().get(userAccessInfo).getUserPlayInfo();

                    // 누적플레이 증가
                    userAchievements.setCumulativePlayCount(userAchievements.getCumulativePlayCount()+1);
                    // 누적 플레이 수 업적
                    if(userAchievements.getCumulativePlayCount()==1){
                        if(!userCollection.getLabelIds().contains(25))userCollection.getLabelIds().add(25);
                        if(!userCollection.getSkinIds().contains(1))userCollection.getSkinIds().add(1);
                        if(!userCollection.getAchievementIds().contains(25))userCollection.getAchievementIds().add(25);
//                        reward.getSkins().add("pastelyellow");
                    }else if(userAchievements.getCumulativePlayCount()==3){
                        if(!userCollection.getLabelIds().contains(26))userCollection.getLabelIds().add(26);
                        if(!userCollection.getSkinIds().contains(2))userCollection.getSkinIds().add(2);
                        if(!userCollection.getAchievementIds().contains(26))userCollection.getAchievementIds().add(26);
//                        reward.getSkins().add("pastelred");
                    }else if(userAchievements.getCumulativePlayCount()==5){
                        if(!userCollection.getLabelIds().contains(27))userCollection.getLabelIds().add(27);
                        if(!userCollection.getSkinIds().contains(3))userCollection.getSkinIds().add(3);
                        if(!userCollection.getAchievementIds().contains(27))userCollection.getAchievementIds().add(27);
//                        reward.getSkins().add("pastelpink");
                    }else if(userAchievements.getCumulativePlayCount()==10){
                        if(!userCollection.getLabelIds().contains(28))userCollection.getLabelIds().add(28);
                        if(!userCollection.getSkinIds().contains(4))userCollection.getSkinIds().add(4);
                        if(!userCollection.getAchievementIds().contains(28))userCollection.getAchievementIds().add(28);
//                        reward.getSkins().add("pastelgreen");
                    }else if(userAchievements.getCumulativePlayCount()==30){
                        if(!userCollection.getLabelIds().contains(29))userCollection.getLabelIds().add(29);
                        if(!userCollection.getAchievementIds().contains(29))userCollection.getAchievementIds().add(29);
                    }else if(userAchievements.getCumulativePlayCount()==50){
                        if(!userCollection.getLabelIds().contains(30))userCollection.getLabelIds().add(30);
                        if(!userCollection.getAchievementIds().contains(30))userCollection.getAchievementIds().add(30);
                    }else if(userAchievements.getCumulativePlayCount()==100) {
                        if(!userCollection.getLabelIds().contains(31))userCollection.getLabelIds().add(31);
                        if(!userCollection.getAchievementIds().contains(31))userCollection.getAchievementIds().add(31);
                    }

                    int rank=userPlayInfo.getRank();
                    // 승패 +1
                    if(rank==1){
                        userAchievements.setCumulativeWinCount(userAchievements.getCumulativeWinCount()+1);
                    }else{
                        userAchievements.setCumulativeLoseCount(userAchievements.getCumulativeLoseCount()+1);
                    }
                    // 승 업적
                    if(userAchievements.getCumulativeWinCount()==1){
                        if(!userCollection.getLabelIds().contains(32))userCollection.getLabelIds().add(32);
                        if(!userCollection.getSkinIds().contains(5))userCollection.getSkinIds().add(5);
                        if(!userCollection.getAchievementIds().contains(32))userCollection.getAchievementIds().add(32);
//                        reward.getSkins().add("pastelblue");
                    }else if(userAchievements.getCumulativeWinCount()==2){
                        if(!userCollection.getLabelIds().contains(33))userCollection.getLabelIds().add(33);
                        if(!userCollection.getSkinIds().contains(7))userCollection.getSkinIds().add(7);
                        if(!userCollection.getAchievementIds().contains(33))userCollection.getAchievementIds().add(33);
//                        reward.getSkins().add("googlered");
                    }else if(userAchievements.getCumulativeWinCount()==3){
                        if(!userCollection.getLabelIds().contains(34))userCollection.getLabelIds().add(34);
                        if(!userCollection.getSkinIds().contains(8))userCollection.getSkinIds().add(8);
                        if(!userCollection.getAchievementIds().contains(34))userCollection.getAchievementIds().add(34);
//                        reward.getSkins().add("googleorange");
                    }else if(userAchievements.getCumulativeWinCount()==5){
                        if(!userCollection.getLabelIds().contains(35))userCollection.getLabelIds().add(35);
                        if(!userCollection.getSkinIds().contains(9))userCollection.getSkinIds().add(9);
                        if(!userCollection.getAchievementIds().contains(35))userCollection.getAchievementIds().add(35);
//                        reward.getSkins().add("googlegreen");
                    }else if(userAchievements.getCumulativeWinCount()==7){
                        if(!userCollection.getLabelIds().contains(36))userCollection.getLabelIds().add(36);
                        if(!userCollection.getSkinIds().contains(10))userCollection.getSkinIds().add(10);
                        if(!userCollection.getAchievementIds().contains(36))userCollection.getAchievementIds().add(36);
//                        reward.getSkins().add("googleblue");
                    }else if(userAchievements.getCumulativeWinCount()==10){
                        if(!userCollection.getLabelIds().contains(37))userCollection.getLabelIds().add(37);
                        if(!userCollection.getSkinIds().contains(28))userCollection.getSkinIds().add(28);
                        if(!userCollection.getAchievementIds().contains(37))userCollection.getAchievementIds().add(37);
//                        reward.getSkins().add("basicyellow-sunglass");
                    }else if(userAchievements.getCumulativeWinCount()==20){
                        if(!userCollection.getLabelIds().contains(38))userCollection.getLabelIds().add(38);
                        if(!userCollection.getAchievementIds().contains(38))userCollection.getAchievementIds().add(38);
                    }
                    // 패 업적
                    if(userAchievements.getCumulativeLoseCount()==1){
                        if(!userCollection.getLabelIds().contains(39))userCollection.getLabelIds().add(39);
                        if(!userCollection.getAchievementIds().contains(39))userCollection.getAchievementIds().add(39);
//                        userCollection.getSkinIds().add(6);
//                        reward.getSkins().add("npcWhite");
                    }else if(userAchievements.getCumulativeLoseCount()==2){
                        if(!userCollection.getLabelIds().contains(40))userCollection.getLabelIds().add(40);
                        if(!userCollection.getSkinIds().contains(24))userCollection.getSkinIds().add(24);
                        if(!userCollection.getAchievementIds().contains(40))userCollection.getAchievementIds().add(40);
//                        reward.getSkins().add("basicgreen-sunglass");
                    }else if(userAchievements.getCumulativeLoseCount()==3){
                        if(!userCollection.getLabelIds().contains(41))userCollection.getLabelIds().add(41);
                        if(!userCollection.getSkinIds().contains(15))userCollection.getSkinIds().add(15);
                        if(!userCollection.getAchievementIds().contains(41))userCollection.getAchievementIds().add(41);
//                        reward.getSkins().add("basicred");
                    }else if(userAchievements.getCumulativeLoseCount()==5){
                        if(!userCollection.getLabelIds().contains(42))userCollection.getLabelIds().add(42);
                        if(!userCollection.getSkinIds().contains(16))userCollection.getSkinIds().add(16);
                        if(!userCollection.getAchievementIds().contains(42))userCollection.getAchievementIds().add(42);
//                        reward.getSkins().add("basicred-sunglass");
                    }else if(userAchievements.getCumulativeLoseCount()==7){
                        if(!userCollection.getLabelIds().contains(43))userCollection.getLabelIds().add(43);
                        if(!userCollection.getAchievementIds().contains(43))userCollection.getAchievementIds().add(43);
                    }else if(userAchievements.getCumulativeLoseCount()==10){
                        if(!userCollection.getLabelIds().contains(44))userCollection.getLabelIds().add(44);
                        if(!userCollection.getAchievementIds().contains(44))userCollection.getAchievementIds().add(44);
                    }

                    // 누적 밟기
                    int step=userPlayInfo.getStep();
                    userAchievements.setStep(userAchievements.getStep()+step);
                    if(userAchievements.getStep()>=100){
                        if(!userCollection.getLabelIds().contains(49))userCollection.getLabelIds().add(49);
                        if(!userCollection.getAchievementIds().contains(49))userCollection.getAchievementIds().add(49);
                    }else if(userAchievements.getStep()>=50){
                        if(!userCollection.getLabelIds().contains(48))userCollection.getLabelIds().add(48);
                        if(!userCollection.getAchievementIds().contains(48))userCollection.getAchievementIds().add(48);
                    }else if(userAchievements.getStep()==20){
                        if(!userCollection.getLabelIds().contains(47))userCollection.getLabelIds().add(47);
                        if(!userCollection.getSkinIds().contains(24))userCollection.getSkinIds().add(24);
                        if(!userCollection.getAchievementIds().contains(47))userCollection.getAchievementIds().add(47);
//                        reward.getSkins().add("basicgreen-sunglass");
                    }else if(userAchievements.getStep()>=10){
                        if(!userCollection.getLabelIds().contains(46))userCollection.getLabelIds().add(46);
                        if(!userCollection.getSkinIds().contains(18))userCollection.getSkinIds().add(18);
                        if(!userCollection.getAchievementIds().contains(46))userCollection.getAchievementIds().add(46);
//                        reward.getSkins().add("basicred-butterfly");
                    }else if(userAchievements.getStep()>=5){
                        if(!userCollection.getLabelIds().contains(45))userCollection.getLabelIds().add(45);
                        if(!userCollection.getSkinIds().contains(17)) userCollection.getSkinIds().add(17);
                        if(!userCollection.getAchievementIds().contains(45))userCollection.getAchievementIds().add(45);
//                        reward.getSkins().add("basicred-magichat");
                    }

                    // 누적 밟히기
                    int stepped=userPlayInfo.getStepped();
                    userAchievements.setStepped(userAchievements.getStepped()+stepped);
                    if(userAchievements.getStepped()>=200){
                        if(!userCollection.getLabelIds().contains(54))userCollection.getLabelIds().add(54);
                        if(!userCollection.getAchievementIds().contains(54))userCollection.getAchievementIds().add(54);
                    }else if(userAchievements.getStepped()>=100){
                        if(!userCollection.getLabelIds().contains(53))userCollection.getLabelIds().add(53);
                        if(!userCollection.getAchievementIds().contains(53))userCollection.getAchievementIds().add(53);
                    }else if(userAchievements.getStepped()>=50){
                        if(!userCollection.getLabelIds().contains(52))userCollection.getLabelIds().add(52);
                        if(!userCollection.getAchievementIds().contains(52))userCollection.getAchievementIds().add(52);
                    }else if(userAchievements.getStepped()>=10){
                        if(!userCollection.getLabelIds().contains(51)) userCollection.getLabelIds().add(51);
                        if(!userCollection.getSkinIds().contains(17))userCollection.getSkinIds().add(17);
                        if(!userCollection.getAchievementIds().contains(51))userCollection.getAchievementIds().add(51);
//                        reward.getSkins().add("basicred-magichat");
                    }else if(userAchievements.getStepped()>=5){
                        if(!userCollection.getLabelIds().contains(50))userCollection.getLabelIds().add(50);
                        if(!userCollection.getAchievementIds().contains(50))userCollection.getAchievementIds().add(50);
                    }

                    // 0데스
                    if(stepped==0){
                        if(!userCollection.getLabelIds().contains(55))userCollection.getLabelIds().add(55);
                        if(!userCollection.getAchievementIds().contains(55))userCollection.getAchievementIds().add(55);
                    }

                    // 아이템 획득 수
                    int itemCount=userPlayInfo.getItemCount();
                    userAchievements.setItemCount(userAchievements.getItemCount()+itemCount);
                    if(userAchievements.getItemCount()>=20){
                        if(!userCollection.getLabelIds().contains(59))userCollection.getLabelIds().add(59);
                        if(!userCollection.getAchievementIds().contains(59))userCollection.getAchievementIds().add(59);
                    }else if(userAchievements.getItemCount()>=10){
                        if(!userCollection.getLabelIds().contains(58))userCollection.getLabelIds().add(58);
                        if(!userCollection.getSkinIds().contains(17))userCollection.getSkinIds().add(17);
                        if(!userCollection.getAchievementIds().contains(58))userCollection.getAchievementIds().add(58);
//                        reward.getSkins().add("basicred-magichat");
                    }else if(userAchievements.getItemCount()>=5){
                        if(!userCollection.getLabelIds().contains(57))userCollection.getLabelIds().add(57);
                        if(!userCollection.getSkinIds().contains(20))userCollection.getSkinIds().add(20);
                        if(!userCollection.getAchievementIds().contains(57))userCollection.getAchievementIds().add(57);
//                        reward.getSkins().add("basicpink-sunglass");
                    }else if(userAchievements.getItemCount()>=1){
                        if(!userCollection.getLabelIds().contains(56))userCollection.getLabelIds().add(56);
                        if(!userCollection.getSkinIds().contains(18))userCollection.getSkinIds().add(18);
                        if(!userCollection.getAchievementIds().contains(56))userCollection.getAchievementIds().add(56);
//                        reward.getSkins().add("basicred-butterfly");
                    }

                    // 누적 플레이타임(초)
                    // long playTime=Duration.between(userPlayInfo.getStartDate(),userPlayInfo.getEndDate()).toMinutes();
                    long playTime=GAME_TIME;
                    userAchievements.setPlaytime(userAchievements.getPlaytime()+playTime);
                    if(userAchievements.getPlaytime()>=36000){
                        if(!userCollection.getLabelIds().contains(63))userCollection.getLabelIds().add(63);
                        if(!userCollection.getAchievementIds().contains(63))userCollection.getAchievementIds().add(63);
                    }else if(userAchievements.getItemCount()>=18000){
                        if(!userCollection.getLabelIds().contains(62))userCollection.getLabelIds().add(62);
                        if(!userCollection.getAchievementIds().contains(62))userCollection.getAchievementIds().add(62);
                    }else if(userAchievements.getItemCount()>=7200){
                        if(!userCollection.getLabelIds().contains(61))userCollection.getLabelIds().add(61);
                        if(!userCollection.getAchievementIds().contains(61))userCollection.getAchievementIds().add(61);
                    }else if(userAchievements.getItemCount()>=3600){
                        if(!userCollection.getLabelIds().contains(60))userCollection.getLabelIds().add(60);
                        if(!userCollection.getAchievementIds().contains(60))userCollection.getAchievementIds().add(60);
                    }

                    // mongo, redis 업데이트
                    rankUtil.updateUserRankRedis(userProfile);
                    rankUtil.getMyRank(userAccessInfo.getUserProfileDto()); // 순위, 티어 계산

                    userAccessInfo.setUserAchievements(userAchievements);
                    userAccessInfo.setUserCollection(userCollection);
                    userAccessInfo.setUserProfile(userProfile);

                    userProfileRepository.save(userProfile);
                    userCollectionRepository.save(userCollection);
                    userAchievementsRepository.save(userAchievements);
                }
            } catch (Exception e){
                System.out.println("ResultSaveError");
                e.printStackTrace();
            }
        },0, TimeUnit.SECONDS);

    }

    private long calExp(long exp, int rank, int size){
        long tmp=exp;
        if (size==2){
            if (rank==1) return tmp+150;
            return tmp+50;
        } else if (size==3){
            if (rank==1) return tmp+225;
            else if (rank==2) return tmp+125;
            return tmp+75;
        } else {
            if (rank==1) return tmp+300;
            else if (rank==2) return tmp+200;
            else if (rank==3) return tmp+150;
            return tmp+100;
        }
//        if (size==2){
//            if (rank==1) return 200;
//            return 100;
//        } else if (size==3){
//            if (rank==1) return 250;
//            else if (rank==2) return 175;
//            return 100;
//        } else {
//            if (rank==1) return 300;
//            else if (rank==2) return 200;
//            else if (rank==3) return 150;
//            return 100;
//        }
    }
    private int calRating(int rating, int rank, int size){
        int tmp = rating;
        if (size==2){
            if (rank==1) tmp += 70;
            else tmp -= 30;
        } else if (size==3){
            if (rank==1) tmp += 80;
            else if (rank==2) tmp += 20;
            else tmp -= 40;
        } else {
            if (rank==1) tmp += 100;
            else if (rank==2) tmp += 40;
            else if (rank==3) tmp += 10;
            else tmp -= 50;
        }
        return Math.max(0, tmp);
    }


    private void gameClose(GameInfo gameInfo){

//        roomUuid있으면 해당 룸으로 보낸다.
        System.out.println("game close");
        gameInfo.setEndDate();

        // 순위 정하기
        PriorityQueue<UserGameInfo> pq = new PriorityQueue<UserGameInfo>((x,y)->{
            if (x.getScore()==y.getScore()){
                return x.getStepOrder()-y.getStepOrder();
            } else return y.getScore()-x.getScore();
        });

        for (UserGameInfo userGameInfo:gameInfo.getUserGameInfoList()){
            if (userGameInfo.getStepOrder()==null){
                userGameInfo.getUserPlayInfo().setRank(gameInfo.getUsers().size());
            } else pq.add(userGameInfo);
        }

        // 경험치, 레이팅 업데이트
        int rank = 1;
        while (!pq.isEmpty()){
            UserGameInfo cur = pq.poll();
            cur.getUserPlayInfo().setRank(rank++);
        }
        for (Map.Entry<UserAccessInfo, UserGameInfo> entry:gameInfo.getUsers().entrySet()){
            UserProfile userProfile = entry.getKey().getUserProfile();
            rank = entry.getValue().getUserPlayInfo().getRank();

            // 갱신 전에 복사본 생성
            UserAccessInfo userAccessInfo = entry.getKey();
            UserAchievements userAchievements=userAccessInfo.getUserAchievements();
//            UserAchievements deepCopyOfAchievements = new UserAchievements(userAchievements);
//            userAccessInfo.getResultInfo().setDeepCopyOfAchievements(deepCopyOfAchievements);

//            System.out.println(userProfile.getUserCode()+" exp 테스트 전 "+userProfile.getUserExp());
            userProfile.setUserExp(calExp(userProfile.getUserExp(),rank, gameInfo.getUsers().size()));
//            System.out.println(userProfile.getUserCode()+" exp 테스트 후 "+userProfile.getUserExp());

            // room으로 처리
            // 매칭일때만 rating 계산해주기
            if(gameInfo.getRoom()==null){
                userProfile.setUserRating(calRating(userProfile.getUserRating(), rank, gameInfo.getUsers().size()));

                userAccessInfo.getUserProfileDto().setRating(userProfile.getUserRating());
            }

        }
        ResultInfo resultInfo = new ResultInfo(gameInfo);

        // userinfo마다 처리한다
        for (Map.Entry<UserAccessInfo, UserGameInfo> entry: gameInfo.getUsers().entrySet()){
            // useraccessinfo를 조회해서 achivement의 깊은 복사본을 만들고 resultinfo에 넣는다
            // datasave에서 사용할 원래 achivement를 갱신함
            // 이 두 차이를 가지고 getresult에서 갱신
            // useraccessinfo에 있는게 최신본
            // 깊은 복사가 과거꺼
            UserAccessInfo userAccessInfo = entry.getKey();
            userAccessInfo.setResultInfo(resultInfo);
        }

        dataSave(gameInfo);
        inGameCollection.removeGame(gameInfo);

    }

}
