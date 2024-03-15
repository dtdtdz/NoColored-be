package com.ssafy.backend.play.util;

import com.ssafy.backend.game.domain.UserAccessInfo;
import com.ssafy.backend.play.domain.MatchingInfo;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MatchingCollection {
    private final List<MatchingInfo>[] matchingQueue;

    private final Map<UserAccessInfo, MatchingInfo> matchingInfoMap;
    private final Queue<MatchingInfo> addQueue;
    private final Queue<MatchingInfo> delQueue;
    public MatchingCollection(){
        matchingQueue = new List[100];
        matchingInfoMap = new LinkedHashMap<>();
        addQueue = new ArrayDeque<>();
        delQueue = new ArrayDeque<>();
        for (int i=0; i<100; i++){
            matchingQueue[i] =new LinkedList<>();
        }
    }
    public synchronized void setMatching(UserAccessInfo userAccessInfo){
        MatchingInfo matchingInfo = new MatchingInfo(userAccessInfo);
        matchingInfoMap.put(userAccessInfo, matchingInfo);
        addQueue.offer(matchingInfo);
    }
    public synchronized void removeMatching(MatchingInfo matchingInfo){
        delQueue.offer(matchingInfo);
    }
    public void matching(){
        
    }

}
