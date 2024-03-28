package com.ssafy.backend.rank.service;

import com.ssafy.backend.rank.dao.RankMongo;
import com.ssafy.backend.rank.dto.RankDto;
import com.ssafy.backend.rank.dto.RankInfoDto;
import com.ssafy.backend.rank.repository.RankRepository;
import com.ssafy.backend.rank.util.RankUtil;
import com.ssafy.backend.user.dao.UserProfileRepository;
import com.ssafy.backend.user.dto.UserProfileDto;
import com.ssafy.backend.user.entity.UserProfile;
import com.ssafy.backend.user.util.JwtUtil;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ssafy.backend.rank.util.RankUtil.tierCalculation;

@Service
public class RankServiceImpl implements RankService{

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtUtil jwtUtil;
    private final UserProfileRepository userProfileRepository;
    private final RankUtil rankUtil;
    private final RankRepository rankRepository;

    public RankServiceImpl(RedisTemplate<String, Object> redisTemplate,
                           JwtUtil jwtUtil, UserProfileRepository userProfileRepository, RankUtil rankUtil, RankRepository rankRepository){
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
        this.userProfileRepository = userProfileRepository;
        this.rankUtil = rankUtil;
        this.rankRepository = rankRepository;
    }

    // userRank 레디스 요소 다 삭제
    @Override
    public ResponseEntity<?> clearRedis(){
        String key="userRank";
        redisTemplate.delete(key);
        return ResponseEntity.ok("userRank 초기화 완료");
    }

    // 더미데이터 넣기
    @Override
    public ResponseEntity<?> putRank(int dataNumber){
        String key="userRank";
        for(int i=1;i<=dataNumber;i++){
            String userCode=Integer.toString(i);
            double rating=(double) i;
            redisTemplate.opsForZSet().add(key,userCode,rating);
        }
        return ResponseEntity.ok(dataNumber+" 만큼 더미 데이터 레디스에 넣기 성공");
    }


    // 상위 100명 랭크 보기
    @Override
    public RankInfoDto getRankList(){
        LocalDateTime refreshTime = LocalDateTime.now();
        List<RankDto> rankDtoList = new ArrayList<>();

        // Redis에서 최대 상위 100명의 유저 정보 가져오기
        // 반환 타입을 Set<String>으로 안전하게 변환
        // Redis에서 전체 요소 수 가져오기
        Long totalElements = redisTemplate.opsForZSet().size("userRank");
        if(totalElements>100){
            totalElements= 100L;
        }
        Set<Object> topRankObjects = redisTemplate.opsForZSet().reverseRange("userRank", 0, totalElements);
        Set<String> topRanks = topRankObjects.stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
        int rank = 0;
        for (String userCode : topRanks) {
            RankDto rankDto = new RankDto(); // MongoDB 조회 결과를 바탕으로 RankDto 생성
            Optional<UserProfile> userProfile=userProfileRepository.findByUserCode(userCode);
            int userRankInt=++rank;
            rankDto.setRank(userRankInt);
            rankDto.setUserCode(userCode);
            rankDto.setNickname(userProfile.get().getUserNickname());
            rankDto.setSkin(userProfile.get().getUserSkin());
            rankDto.setLabel(userProfile.get().getUserLabel());
            int rating=userProfile.get().getUserRating();
            rankDto.setRating(rating);
            rankDto.setTier(rankUtil.tierCalculation(rank,rating));
            // 여기에 MongoDB에서 조회한 정보를 rankDto에 설정하는 로직 추가
            rankDtoList.add(rankDto);
        }
        return new RankInfoDto(refreshTime, rankDtoList);
    }

    // 내 랭크 보기
    @Override
    public UserProfileDto getRank(UserAccessInfo user) {
        UserProfile userProfile=user.getUserProfile();
        String userCode=userProfile.getUserCode();
        UserProfileDto userProfileDto=user.getUserProfileDto();
        // 게스트는 랭킹 조회 안됌
        if(userProfile.isGuest()){
            return null;
        }
        rankUtil.getMyRank(userProfileDto);

//        // 프로필에 레이팅 점수 저장
//        userProfile.setUserRating(userRating);
//        userProfileRepository.save(userProfile);
        return userProfileDto;
    }

}
