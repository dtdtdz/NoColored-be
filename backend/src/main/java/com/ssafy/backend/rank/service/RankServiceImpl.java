package com.ssafy.backend.rank.service;

import com.ssafy.backend.rank.dao.RankMongo;
import com.ssafy.backend.rank.dto.RankDto;
import com.ssafy.backend.rank.dto.RankInfoDto;
import com.ssafy.backend.rank.repository.RankRepository;
import com.ssafy.backend.rank.util.RankUtil;
import com.ssafy.backend.user.dao.UserProfileRepository;
import com.ssafy.backend.user.entity.UserProfile;
import com.ssafy.backend.user.util.JwtUtil;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    // 상위 100명 랭크 보기
    @Override
    public RankInfoDto getRankList(){
        LocalDateTime refreshTime = LocalDateTime.now();
        List<RankDto> rankDtoList = new ArrayList<>();

        // Redis에서 상위 100명의 유저 정보 가져오기
        // 반환 타입을 Set<String>으로 안전하게 변환
        Set<Object> topRankObjects = redisTemplate.opsForZSet().reverseRange("userRank", 0, 99);
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
            rankDto.setLabel(userProfile.get().getUserTitle());
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
    public RankDto getRank(String token) {
        UserAccessInfo user = jwtUtil.getUserAccessInfoRedis(token);
        UserProfile userProfile=user.getUserProfile();
        String userCode=userProfile.getUserCode();
        // 레디스에서 사용자의 점수(score)와 등수(rank) 조회
        String key = "userRank";
        Double score = redisTemplate.opsForZSet().score(key, userCode);
        Long rank = redisTemplate.opsForZSet().reverseRank(key, userCode);

        // 점수(score)가 null인 경우, 사용자가 랭킹에 없는 것으로 간주하고 초기 값을 설정
        int userScore = (score != null) ? score.intValue() : 0;
        // 등수(rank)는 0부터 시작하므로 실제 등수를 얻기 위해 +1
        int userRank = (rank != null) ? rank.intValue() + 1 : -1; // 랭킹에 없는 경우 -1로 설정

        // 프로필에 레이팅 점수 저장
        userProfile.setUserRating(userScore);
        userProfileRepository.save(userProfile);

        // mongodb에도 레이팅 점수 저장
//        Optional<RankMongo> rankMongoOptional=rankRepository.findById(userProfile.getUserCode());
//        if(rankMongoOptional.isPresent()){
//            RankMongo rankMongo=rankMongoOptional.get();
//            rankMongo.setRating(userScore);
//        }

        // UserProfile에서 나머지 필요한 정보를 가져와 RankDto 객체 생성
        return RankDto.builder()
                .rank(userRank)
                .userCode(userCode)
                .nickname(userProfile.getUserNickname())
                .rating(userScore)
                .skin(userProfile.getUserSkin())
                .label(userProfile.getUserTitle())
                .tier(rankUtil.tierCalculation(userRank,userScore))
                .build();
    }

}
