package com.ssafy.backend.play.service;

import com.ssafy.backend.play.domain.RoomInfo;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import com.ssafy.backend.play.util.MatchingCollection;
import com.ssafy.backend.user.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        try {
            matchingCollection.setAddMatching(userAccessInfo);
        } catch (Exception e){
            if (userAccessInfo.getPosition() instanceof RoomInfo){
                throw new RuntimeException("Position conflict: "
                        +userAccessInfo.getRoomInfo().getClass()+":"
                        +userAccessInfo.getRoomInfo().getRoomDto().getRoomCode());
            } else {
                throw new RuntimeException("Position conflict: "
                        +userAccessInfo.getRoomInfo().getClass());
            }
        }

//        return false;
    }

    @Override
    public void delMatchingList(String token) {
        UserAccessInfo userAccessInfo = jwtUtil.getUserAccessInfoRedis(token);
        matchingCollection.setDelMatching(userAccessInfo);
    }

}
