package com.ssafy.backend.play.domain;

import com.ssafy.backend.websocket.domain.UserAccessInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchingInfo {
    private UserAccessInfo userAccessInfo;
    private long startTime;
    private int ratingLevel;
    private int expandLevel;
    public MatchingInfo(UserAccessInfo userAccessInfo){
        this.userAccessInfo = userAccessInfo;
        startTime = System.currentTimeMillis();
        ratingLevel = Math.min(99, userAccessInfo.getUserProfile().getUserRating()/100);
        expandLevel = -1;
    }
}
