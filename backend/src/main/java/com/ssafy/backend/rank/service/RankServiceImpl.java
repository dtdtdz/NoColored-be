package com.ssafy.backend.rank.service;

import com.ssafy.backend.rank.dto.RankDto;
import com.ssafy.backend.rank.dto.RankInfoDto;
import com.ssafy.backend.rank.repository.RankRepository;
import com.ssafy.backend.rank.util.RankUtil;
import com.ssafy.backend.user.repository.UserProfileRepository;
import com.ssafy.backend.user.dto.UserProfileDto;
import com.ssafy.backend.user.entity.UserProfile;
import com.ssafy.backend.user.util.JwtUtil;
import com.ssafy.backend.websocket.domain.UserAccessInfo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
            String userCode="dummy "+i;
            double rating= 3000 + i*10;

            // 더미 userprofile
            UserProfile userProfile=UserProfile.builder()
                    .id(UUID.randomUUID())
                    .userCode(userCode)
                    .userNickname("dumni "+i)
                    .isGuest(false)
                    .userExp(1000L)
                    .userSkin("https://nocolored.s3.ap-northeast-2.amazonaws.com/character-240px-sheet-basicblue.png")
                    .userLabel("파릇파릇 새싹")
                    .userRating((int) rating)
                    .build();
            userProfileRepository.save(userProfile);

            // redis에 넣기
            redisTemplate.opsForZSet().add(key,userCode,rating);
        }
        return ResponseEntity.ok(dataNumber+" 만큼 더미 데이터 레디스에 넣기 성공");
    }

    // 상위 100명 랭크 보기
    @Override
    public RankInfoDto getRankList(){
        LocalDateTime refreshTime = LocalDateTime.now();
        List<RankDto> players = new ArrayList<>();

        List<String> topRanks = Objects.requireNonNull(redisTemplate.opsForZSet()
                        .reverseRange("userRank", 0, 99)) // 최대 100명까지 직접 지정
                .stream()
                .map(Object::toString)
                .toList(); // 순서 보장을 위해 List 사용

        AtomicInteger myRank = new AtomicInteger(0);
        for (String userCode : topRanks) {
            Double score = redisTemplate.opsForZSet().score("userRank", userCode);
            int rating= score.intValue();
            System.out.println(userCode+" 점수 : "+rating);

            userProfileRepository.findByUserCode(userCode).ifPresent(userProfile -> {
                RankDto rankDto = RankDto.builder()
                        .rank(myRank.incrementAndGet())
                        .userCode(userCode)
                        .nickname(userProfile.getUserNickname())
                        .skin(userProfile.getUserSkin())
                        .label(userProfile.getUserLabel())
                        .rating(rating)
                        .tier(tierCalculation(myRank.get(),rating, userProfile.getUserExp()))
                        .build();
                players.add(rankDto);
            });
        }
        return new RankInfoDto(refreshTime, players);
    }

    // 내 랭크 보기
    @Override
    public UserProfileDto getRank(UserAccessInfo user) {
        UserProfile userProfile=user.getUserProfile();
        // 게스트는 랭킹 조회 안됌
        if(userProfile.isGuest()){
            return null;
        }
        rankUtil.getMyRank(user.getUserProfileDto());
        return user.getUserProfileDto();
    }

}
