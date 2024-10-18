package com.ssafy.backend.play.util;

import com.ssafy.backend.assets.SynchronizedSend;
import com.ssafy.backend.game.domain.GameInfo;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import com.ssafy.backend.game.util.InGameCollection;
import com.ssafy.backend.play.domain.MatchingInfo;
import com.ssafy.backend.websocket.domain.SendTextMessageType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 매칭 로직을 처리하는 서비스 클래스
 * 스프링 스케줄러로 매칭을 0.5s마다 진행
 * 매칭 진행 전에 매칭 진입, 취소 요청을 우선 처리
 * 하나의 컬렉션을 동시에 접근하면 에러가 발생하므로 sychronized로 락을 걸고 작업
 * 매칭큐를 0~99단계로 만들고 현재점수/100에 해당하는 단계에 진입
 * 0.5s 마다 1단계씩 위아래로 확장시켜 진입
 * 높은 점수대에서 부터, 오래 기다린 유저 기준으로 매칭 인원을 만족하면 게임을 생성
 * 매칭인원: 기본: 4인, 7초 대기: 3인, 12초 대기: 2인
 */
@Component
public class MatchingCollection {
    private final List<List<UserAccessInfo>> matchingQueue;
    private final Map<UserAccessInfo, MatchingInfo> matchingInfoMap;
    private final Queue<UserAccessInfo> addQueue;
    private final Queue<UserAccessInfo> delQueue;
    private final InGameCollection inGameCollection;
    public MatchingCollection(InGameCollection inGameCollection){
        this.inGameCollection = inGameCollection;
        matchingQueue = new ArrayList<>();
        matchingInfoMap = new LinkedHashMap<>();
        addQueue = new ArrayDeque<>();
        delQueue = new ArrayDeque<>();
        for (int i=0; i<100; i++){
            matchingQueue.add(new LinkedList<>());
        }
    }
    public void setAddMatching(UserAccessInfo userAccessInfo){
        MatchingInfo matchingInfo = new MatchingInfo(userAccessInfo);
        userAccessInfo.setMatchingInfo(matchingInfo);
        synchronized (addQueue){
            addQueue.offer(userAccessInfo);
        }
    }

    public void setDelMatching(UserAccessInfo userAccessInfo){
        synchronized (delQueue){
            delQueue.offer(userAccessInfo);
        }
    }
    private void delMatchingList(){
        synchronized (delQueue) {
            while (!delQueue.isEmpty()) {
                UserAccessInfo userAccessInfo = delQueue.poll();
                delMatching(userAccessInfo);
                userAccessInfo.clearPosition();
                WebSocketSession session = userAccessInfo.getSession();
                SynchronizedSend.textSend(session,
                        SendTextMessageType.MATCHING_CANCEL.getValue(), null);
            }
        }
    }

    private void delMatching(UserAccessInfo userAccessInfo) {
        if (!matchingInfoMap.containsKey(userAccessInfo)) return;
        MatchingInfo matchingInfo = matchingInfoMap.get(userAccessInfo);
        for (List<UserAccessInfo> list:matchingQueue){
            list.remove(userAccessInfo);
        }
        matchingInfoMap.remove(userAccessInfo);
    }

    @Scheduled(fixedRate = 500)
    private void matching(){
        delMatchingList();
        synchronized (addQueue) {
            while (!addQueue.isEmpty()) {
                UserAccessInfo userAccessInfo = addQueue.poll();
                matchingInfoMap.put(userAccessInfo, (MatchingInfo)userAccessInfo.getPosition());
                MatchingInfo matchingInfo = matchingInfoMap.get(userAccessInfo);
                matchingQueue.get(matchingInfo.getRatingLevel()).add(userAccessInfo);
                matchingInfo.setExpandLevel(0);
            }
        }

        long now = System.currentTimeMillis();
        //접속중인 유저를 매칭 리스트에 추가한다.
        if (!matchingInfoMap.isEmpty())
            System.out.println("matching size:"+ matchingInfoMap.size());
        for (MatchingInfo matchingInfo: matchingInfoMap.values()){
;
            if (matchingInfo.getUserAccessInfo().getSession()==null || !matchingInfo.getUserAccessInfo().getSession().isOpen()) {
                setDelMatching(matchingInfo.getUserAccessInfo());
                continue;
            }
            int expandLevel = matchingInfo.getExpandLevel();
            int ratingLevel = matchingInfo.getRatingLevel();
            int timeDiff = (int)((now - matchingInfo.getStartTime())/500);
            //매칭 리스트에 추가, 시간이 진행되면 timeDiff만큼 expandLevel을 확장시킨다.
            while (timeDiff > expandLevel && expandLevel<matchingQueue.size()){
                expandLevel++;
                if (ratingLevel-expandLevel>=0){
                    matchingQueue.get(ratingLevel-expandLevel).add(matchingInfo.getUserAccessInfo());
                }
                if (ratingLevel+expandLevel<matchingQueue.size()){
                    matchingQueue.get(ratingLevel+expandLevel).add(matchingInfo.getUserAccessInfo());
                }
            }
            matchingInfo.setExpandLevel(expandLevel);
        }
        delMatchingList();

        //높은 점수대부터 매칭 시도
        //7초 3인, 12초 2인 매칭가능
        for (int i=matchingQueue.size()-1; i>=0; i--){
            while (!matchingQueue.get(i).isEmpty()
                    && matchingQueue.get(i).size() >= getMatchingSize(matchingQueue.get(i),now)){
                List<UserAccessInfo> list = new ArrayList<>();

                int size = getMatchingSize(matchingQueue.get(i),now);
                if (size==Integer.MAX_VALUE) continue;
                for (int j=0; j<size; j++){
                    try {
                        list.add(matchingQueue.get(i).get(0));
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    delMatching(matchingQueue.get(i).get(0));
                }

                for (UserAccessInfo userAccessInfo:list){
                    SynchronizedSend.textSend(userAccessInfo.getSession(), SendTextMessageType.MATCHING.getValue(), null);
                }
                inGameCollection.addGame(list);
            }
        }
    }

    private int getMatchingSize(List<UserAccessInfo> userAccessInfos, long now){
        MatchingInfo matchingInfo = matchingInfoMap.get(userAccessInfos.get(0));

        if (matchingInfo==null){
            delMatching(userAccessInfos.get(0));
            if (userAccessInfos.get(0).getPosition() instanceof MatchingInfo)
                userAccessInfos.get(0).clearPosition();
            return Integer.MAX_VALUE;
        }

        long start = matchingInfo.getStartTime();
        if (now-start<7000){
            return GameInfo.MAX_PLAYER;
        } else if (now-start<12000){
            return 3;
        } else return 2;
    }
}
