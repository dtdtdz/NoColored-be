package com.ssafy.backend.rank.service;

import com.ssafy.backend.rank.dto.RankDto;
import com.ssafy.backend.rank.dto.RankInfoDto;
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

    public RankServiceImpl(RedisTemplate<String, Object> redisTemplate,
                           JwtUtil jwtUtil, UserProfileRepository userProfileRepository, RankUtil rankUtil){
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
        this.userProfileRepository = userProfileRepository;
        this.rankUtil = rankUtil;
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
            int userRank=++rank;
            rankDto.setRank(userRank);
            rankDto.setUserCode(userCode);
            rankDto.setNickname(userProfile.get().getUserNickname());
            rankDto.setSkin(userProfile.get().getUserSkin());
            rankDto.setTitle(userProfile.get().getUserTitle());
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

        return new RankDto();
    }

}
