package com.ssafy.backend.game.service;

import com.ssafy.backend.game.domain.ResultInfo;
import com.ssafy.backend.game.dto.ResultDto;
import com.ssafy.backend.game.util.InGameCollection;
import com.ssafy.backend.game.util.InGameLogic;
import com.ssafy.backend.user.entity.UserProfile;
import com.ssafy.backend.user.repository.UserProfileRepository;
import com.ssafy.backend.user.util.JwtUtil;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import com.ssafy.backend.websocket.util.SessionCollection;
import com.ssafy.backend.game.domain.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

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
    GameServiceImpl(@Qualifier("scheduledExecutorService")ScheduledExecutorService scheduledExecutorService,
                    @Qualifier("authScheduledExecutorService")ScheduledExecutorService saveScheduledExecutorService,
                    UserProfileRepository userProfileRepository,
                    SessionCollection sessionRepository,
                    InGameCollection inGameRepository,
                    InGameLogic inGameLogic,
                    JwtUtil jwtUtil){
        this.scheduledExecutorService = scheduledExecutorService;
        this.saveScheduledExecutorService = saveScheduledExecutorService;
        this.userProfileRepository = userProfileRepository;
        this.sessionCollection = sessionRepository;
        this.inGameCollection = inGameRepository;
        this.inGameLogic = inGameLogic;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public synchronized GameRoomDto ready(String token) {
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        GameInfo gameInfo = userAccessInfo.getGameInfo();
        if (gameInfo==null) return null;
//        gameInfo.getUsers().get(userAccessInfo.getSession()).setAccess(true);
        return gameInfo.getGameRoomDto();
    }

    // resultdto를 만들어줌 그래서 그거 가지고 유저 업적을 할 수 있음
    @Override
    public ResultDto getResult(String token) {
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        ResultDto userResultDto = new ResultDto(userAccessInfo.getResultInfo());
        if (userAccessInfo.getResultInfo().getGameInfo().getRoom()!=null){
            System.out.println(userAccessInfo.getResultInfo().getGameInfo().getRoom().getRoomDto().getRoomTitle());
            userAccessInfo.setRoomInfo(userAccessInfo.getResultInfo().getGameInfo().getRoom());
        } else {
            userAccessInfo.clearPosition();
        }
        return userResultDto;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void scheduleTaskAfterStartup() {
        long initialDelay = 0; // 시작 지연 없음
        long period = 16_666; // 17ms
        
        // 여기에 반복 실행할 태스크의 로직을 작성
        future = scheduledExecutorService.scheduleAtFixedRate(this::gameLogic, initialDelay, period, TimeUnit.MICROSECONDS);
    }
//    스프링 끝나면 스케줄러에서 함수 제거, 필요없긴하다.
//    @EventListener
//    public void onApplicationEvent(ContextClosedEvent event) {
//        future.cancel(false);
//    }

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

                        //        play(gameInfo);

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

                    userProfileRepository.save(userProfile);
                }
            } catch (Exception e){
                System.out.println("ResultSaveError");
                e.printStackTrace();
            }
        },0, TimeUnit.SECONDS);

    }

    private long calExp(long exp, int rank, int size){
        if (size==2){
            if (rank==1) return 200;
            return 100;
        } else if (size==3){
            if (rank==1) return 250;
            else if (rank==2) return 175;
            return 100;
        } else {
            if (rank==1) return 300;
            else if (rank==2) return 200;
            else if (rank==3) return 150;
            return 100;
        }
    }
    private int calRating(int rating, int rank, int size){
        int tmp = rating;
        if (size==2){
            if (rank==1) tmp = 70;
            else tmp = -30;
        } else if (size==3){
            if (rank==1) tmp = 80;
            else if (rank==2) tmp = 20;
            else tmp = -40;
        } else {
            if (rank==1) tmp = 100;
            else if (rank==2) tmp = 200;
            else if (rank==3) tmp = 150;
            else tmp = 100;
        }

        return Math.max(0, tmp);
    }


    private void gameClose(GameInfo gameInfo){
//        roomUuid있으면 해당 룸으로 보낸다.
        System.out.println("game close");

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

        int rank = 1;
        while (!pq.isEmpty()){
            UserGameInfo cur = pq.poll();
            cur.getUserPlayInfo().setRank(rank++);
        }
        for (Map.Entry<UserAccessInfo, UserGameInfo> entry:gameInfo.getUsers().entrySet()){
            UserProfile userProfile = entry.getKey().getUserProfile();
            rank = entry.getValue().getUserPlayInfo().getRank();
            userProfile.setUserExp(calExp(userProfile.getUserExp(),rank, gameInfo.getUsers().size()));
            userProfile.setUserRating(calRating(userProfile.getUserRating(), rank, gameInfo.getUsers().size()));

        }
        ResultInfo resultInfo = new ResultInfo(gameInfo);

        // userinfo마다 처리한다
        for (Map.Entry<UserAccessInfo, UserGameInfo> entry: gameInfo.getUsers().entrySet()){
            UserAccessInfo userAccessInfo = entry.getKey();

            userAccessInfo.setResultInfo(resultInfo);
        }

        dataSave(gameInfo);
        inGameCollection.removeGame(gameInfo);
    }

}
