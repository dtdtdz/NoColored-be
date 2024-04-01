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
            System.out.print("매칭시도..:");
            matchingCollection.setAddMatching(userAccessInfo);
            System.out.println("매칭성공");
        } catch (Exception e){
            System.out.println("매칭실패");
            if (userAccessInfo.getPosition() instanceof RoomInfo){
                throw new RuntimeException("Position conflict: "
                        +userAccessInfo.getRoomInfo().getClass()+":"
                        +userAccessInfo.getRoomInfo().getRoomDto().getRoomCode());
            } else {
                e.printStackTrace();
                throw new RuntimeException("Position conflict: "
                        +userAccessInfo.getPosition().getClass());
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
