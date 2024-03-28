package com.ssafy.backend.rank.util;

import com.ssafy.backend.rank.dao.RankMongo;
import com.ssafy.backend.rank.repository.RankRepository;
import com.ssafy.backend.user.dto.UserProfileDto;
import com.ssafy.backend.user.entity.UserProfile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RankUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RankRepository rankRepository;

    public RankUtil(RedisTemplate<String, Object> redisTemplate, RankRepository rankRepository) {
        this.redisTemplate = redisTemplate;
        this.rankRepository = rankRepository;
    }

    // userRank 레디스에 데이터 생성하기
    public void createUserRankRedis(UserProfileDto userProfileDto){
        String key="userRank";
        String userCode=userProfileDto.getUserCode();
        int rating=userProfileDto.getRating();
        redisTemplate.opsForZSet().add(key,userCode,(double)rating);
    }

    // 게임 끝나고 호출함!
    // userprofile의 rating은 로그아웃할때 갱신
    // userprofile을 가지고 몽고 안에 있는 레이팅도 갱신함
    // 그 후 회원이면 레디스 안에 있는 rating을 갱신한다
    // plus 점수를 받았다고 가정함
    public void updateUserRankRedis(UserProfile userProfile, int plusRating){
        String key="userRank";
        String userCode=userProfile.getUserCode();
        int currentRating=userProfile.getUserRating();
        // mongo값 갱신
        Optional<RankMongo> rankMongoOptional=rankRepository.findById(userCode);
        if(rankMongoOptional.isPresent()){
            RankMongo rankMongo=rankMongoOptional.get();
            rankMongo.setRating(rankMongo.getRating()+plusRating);
            rankRepository.save(rankMongo);
        }
        // 게스트면 데이터 갱신 안함
        if(userProfile.isGuest()){
            return;
        }
        // Redis에서 현재 사용자의 점수를 조회
        Double currentScore = redisTemplate.opsForZSet().score(key, userCode);
        if (currentScore == null) {
            // Redis에 사용자 점수가 없는 경우, UserProfile의 점수를 기준으로 사용
            currentScore = (double) currentRating;
        }
        // 현재 시간(밀리세컨드)의 역수를 점수에 반영
        long currentTimeMillis = System.currentTimeMillis();
        // 시간 역수를 스코어에 적용하여 유니크한 스코어 생성
        double newRating= currentScore+(double) plusRating;
        double timeFactor = 1.0 / currentTimeMillis;
        newRating+=timeFactor;
        redisTemplate.opsForZSet().add(key,userCode,newRating);
    }

    // 등수랑 레이팅 가지고 티어 계산
    public static String tierCalculation(int rank, int rating){
        if(rank<2){
            return "origin";
        }else if(rank<=5){
            return "rgb";
        }else if(rank<=10){
            return "colored";
        }else{
            if(rating<=2000){
                return "nocolored";
            }else if(rating<=2500){
                return "bronze";
            }else if(rating<=3000){
                return "silver";
            }else if(rating<=3500){
                return "gold";
            }else if(rating<=4200){
                return "platinum";
            }else {
                return "diamond";
            }
        }
    }


    public void getMyRank(UserProfileDto userProfileDto){
        String userCode=userProfileDto.getUserCode();
        // 레디스에서 사용자의 점수(rating)와 등수(rank) 조회
        String key = "userRank";
        Double rating = redisTemplate.opsForZSet().score(key, userCode);
        Long rank = redisTemplate.opsForZSet().reverseRank(key, userCode);

        // 점수(score)가 null인 경우, 사용자가 랭킹에 없는 것으로 간주하고 초기 값을 설정
        int userRating= (rating != null) ? rating.intValue() : 0;
        // 등수(rank)는 0부터 시작하므로 실제 등수를 얻기 위해 +1
        int userRank = (rank != null) ? rank.intValue() + 1 : -1; // 랭킹에 없는 경우 -1로 설정

        // userProfileDto 갱신(순위, 랭킹점수, 티어)
        userProfileDto.setRank(userRank);
        userProfileDto.setTier(tierCalculation(userRank,userRating));
    }
}
