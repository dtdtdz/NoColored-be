package com.ssafy.backend.play.util;

import com.ssafy.backend.game.domain.GameInfo;
import com.ssafy.backend.game.domain.UserAccessInfo;
import com.ssafy.backend.play.domain.MatchingInfo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class MatchingCollection {
    private final List<UserAccessInfo>[] matchingQueue;
    private final Map<UserAccessInfo, MatchingInfo> matchingInfoMap;
    private final Queue<UserAccessInfo> addQueue;
    private final Queue<UserAccessInfo> delQueue;
    public MatchingCollection(){
        matchingQueue = new List[100];
        matchingInfoMap = new LinkedHashMap<>();
        addQueue = new ConcurrentLinkedQueue<>();
        delQueue = new ConcurrentLinkedQueue<>();
        for (int i=0; i<100; i++){
            matchingQueue[i] =new LinkedList<>();
        }
    }
    public void setAddMatching(UserAccessInfo userAccessInfo){
//        System.out.println("set!");
        MatchingInfo matchingInfo = new MatchingInfo(userAccessInfo);
        matchingInfoMap.put(userAccessInfo, matchingInfo);
        addQueue.offer(userAccessInfo);
    }
    public void setDelMatching(UserAccessInfo userAccessInfo){
        delQueue.offer(userAccessInfo);
    }
    private void delMatchingList(){
        while (!delQueue.isEmpty()){
            UserAccessInfo userAccessInfo = delQueue.poll();
            delMatching(userAccessInfo);
        }
    }

    private void delMatching(UserAccessInfo userAccessInfo) {
        if (!matchingInfoMap.containsKey(userAccessInfo)) return;
        MatchingInfo matchingInfo = matchingInfoMap.get(userAccessInfo);
        int high = Math.min(matchingQueue.length-1, matchingInfo.getRatingLevel() + matchingInfo.getExpandLevel());
        int low = Math.max(0, matchingInfo.getRatingLevel()-matchingInfo.getExpandLevel());
//        System.out.println(high+" "+low);
        for (int i=low; i<=high; i++){
            matchingQueue[i].remove(userAccessInfo);
        }
        matchingInfoMap.remove(userAccessInfo);
    }

    @Scheduled(fixedRate = 500)
    private void matching(){
        delMatchingList();
        while (!addQueue.isEmpty()){
            UserAccessInfo userAccessInfo = addQueue.poll();
            if (matchingInfoMap.containsKey(userAccessInfo)){
                MatchingInfo matchingInfo = matchingInfoMap.get(userAccessInfo);
                matchingQueue[matchingInfo.getRatingLevel()].add(userAccessInfo);
                matchingInfo.setExpandLevel(0);
            }
        }

        long now = System.currentTimeMillis();
        //접속중인 유저를 매칭 리스트에 추가한다.
        for (MatchingInfo matchingInfo: matchingInfoMap.values()){

            if (!matchingInfo.getUserAccessInfo().getSession().isOpen()) {
                setDelMatching(matchingInfo.getUserAccessInfo());
                continue;
            }
            int expandLevel = matchingInfo.getExpandLevel();
            int ratingLevel = matchingInfo.getRatingLevel();
            int timeDiff = (int)((now - matchingInfo.getStartTime())/500);
            //매칭 리스트에 추가
            while (timeDiff < expandLevel&&expandLevel<matchingQueue.length){
                expandLevel++;
                if (ratingLevel-expandLevel>=0){
                    matchingQueue[ratingLevel-expandLevel].add(matchingInfo.getUserAccessInfo());
                }
                if (ratingLevel+expandLevel<matchingQueue.length){
                    matchingQueue[ratingLevel+expandLevel].add(matchingInfo.getUserAccessInfo());
                }
            }
        }
        delMatchingList();

        //높은 점수대부터 매칭 시도
        for (int i=matchingQueue.length-1; i>=0; i--){
            while (matchingQueue[i].size() >= GameInfo.MAX_PLAYER){

                for (int j=0; j<GameInfo.MAX_PLAYER; j++){
                    delMatching(matchingQueue[i].get(0));
                }
                System.out.println("matching success");
            }
        }
    }
}
