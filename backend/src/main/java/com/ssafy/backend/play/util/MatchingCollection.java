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
//        System.out.println("set!");
        MatchingInfo matchingInfo = new MatchingInfo(userAccessInfo);
        userAccessInfo.setMatchingInfo(matchingInfo);
        matchingInfoMap.put(userAccessInfo, matchingInfo);
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
        int high = Math.min(matchingQueue.size()-1, matchingInfo.getRatingLevel() + matchingInfo.getExpandLevel());
        int low = Math.max(0, matchingInfo.getRatingLevel()-matchingInfo.getExpandLevel());
//        System.out.println(high+" "+low);
        for (int i=low; i<=high; i++){
            matchingQueue.get(i).remove(userAccessInfo);
        }
        matchingInfoMap.remove(userAccessInfo);
    }

    @Scheduled(fixedRate = 500)
    private void matching(){
        delMatchingList();
        synchronized (addQueue) {
            while (!addQueue.isEmpty()) {
                UserAccessInfo userAccessInfo = addQueue.poll();
                if (matchingInfoMap.containsKey(userAccessInfo)) {
                    MatchingInfo matchingInfo = matchingInfoMap.get(userAccessInfo);
                    matchingQueue.get(matchingInfo.getRatingLevel()).add(userAccessInfo);

                    matchingInfo.setExpandLevel(0);
                }
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
            //매칭 리스트에 추가
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

            if (!matchingQueue.get(i).isEmpty())
                System.out.println(i+":"+matchingQueue.get(i).size());
            while (!matchingQueue.get(i).isEmpty() && matchingQueue.get(i).size() >= getMatchingSize(matchingQueue.get(i),now)){
                List<UserAccessInfo> list = new ArrayList<>();
                int size = getMatchingSize(matchingQueue.get(i),now);

                for (int j=0; j<size; j++){
                    try {
                        list.add(matchingQueue.get(i).get(0));
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    delMatching(matchingQueue.get(i).get(0));
                }

//                RoomDto roomDto = new RoomDto(list);
                for (UserAccessInfo userAccessInfo:list){
                    SynchronizedSend.textSend(userAccessInfo.getSession(), SendTextMessageType.MATCHING.getValue(), null);
                }
                inGameCollection.addGame(list);
//                System.out.println(SendTextMessageType.MATCHING.getValue());
            }
        }
    }

    private int getMatchingSize(List<UserAccessInfo> userAccessInfos, long now){
        MatchingInfo matchingInfo = matchingInfoMap.get(userAccessInfos.get(0));
        long start = matchingInfo.getStartTime();
        if (now-start<7000){
            return GameInfo.MAX_PLAYER;
        } else if (now-start<12000){
            return 3;
        } else return 2;
    }
}
