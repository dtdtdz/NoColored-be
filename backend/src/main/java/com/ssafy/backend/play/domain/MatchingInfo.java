package com.ssafy.backend.play.domain;

import com.ssafy.backend.game.domain.UserAccessInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchingInfo {
    UserAccessInfo userAccessInfo;
    long startTime;
    int expandLevel;
    MatchingInfo(UserAccessInfo userAccessInfo){
        this.userAccessInfo = userAccessInfo;
        startTime = System.currentTimeMillis();
        expandLevel = -1;
    }
}
