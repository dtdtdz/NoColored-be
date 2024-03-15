package com.ssafy.backend.play.domain;

import com.ssafy.backend.game.domain.UserAccessInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchingInfo {
    private UserAccessInfo userAccessInfo;
    private long startTime;
    private int expandLevel;
    public MatchingInfo(UserAccessInfo userAccessInfo){
        this.userAccessInfo = userAccessInfo;
        startTime = System.currentTimeMillis();
        expandLevel = -1;
    }
}
