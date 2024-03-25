package com.ssafy.backend.game.service;

import com.ssafy.backend.game.util.InGameCollection;
import com.ssafy.backend.game.util.InGameLogic;
import com.ssafy.backend.user.util.JwtUtil;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import com.ssafy.backend.websocket.util.SessionCollection;
import com.ssafy.backend.game.domain.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

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
//    private final ScheduledExecutorService
    private final SessionCollection sessionCollection;
    private final InGameCollection inGameCollection;
    private final InGameLogic inGameLogic;
    private final JwtUtil jwtUtil;
    private ScheduledFuture<?> future;
    GameServiceImpl(@Qualifier("scheduledExecutorService")ScheduledExecutorService scheduledExecutorService,
                    SessionCollection sessionRepository,
                    InGameCollection inGameRepository,
                    InGameLogic inGameLogic,
                    JwtUtil jwtUtil){
        this.scheduledExecutorService = scheduledExecutorService;
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
        gameInfo.getUsers().get(userAccessInfo.getSession()).setAccess(true);
        return gameInfo.getGameRoomDto();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void scheduleTaskAfterStartup() {
        long initialDelay = 0; // 시작 지연 없음
        long period = 16_666; // 17ms
        
        // 여기에 반복 실행할 태스크의 로직을 작성
        //            System.out.println("태스크 실행: " + System.currentTimeMillis());
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
//                개선필요
                if (Duration.between(gameInfo.getStartDate(), LocalDateTime.now()).getSeconds()>=100){
                    System.out.println("game close");
                    for (Map.Entry<WebSocketSession, UserGameInfo> entry: gameInfo.getUsers().entrySet()){
                        inGameCollection.inGameUser.remove(entry.getKey());
                    }

                    inGameCollection.removeGame(gameInfo);

                } else {
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
            }

        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void gameClose(GameInfo gameInfo){

    }

}
