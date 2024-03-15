package com.ssafy.backend.play.service;

import com.ssafy.backend.game.domain.UserAccessInfo;
import com.ssafy.backend.play.util.MatchingCollection;
import com.ssafy.backend.user.util.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class RankingServiceImpl implements RankingService{

    private final MatchingCollection matchingCollection;
    private final JwtUtil jwtUtil;
    public RankingServiceImpl(MatchingCollection matchingCollection,
                              JwtUtil jwtUtil){
        this.matchingCollection = matchingCollection;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void addMatchingList(String token) {
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        matchingCollection.setAddMatching(userAccessInfo);
//        return false;
    }

    @Override
    public void delMatchingList(String token) {
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        matchingCollection.setDelMatching(userAccessInfo);
    }

}
