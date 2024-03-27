package com.ssafy.backend.rank.util;

import com.ssafy.backend.rank.dao.RankMongo;
import com.ssafy.backend.rank.repository.RankRepository;
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

    // saverankdata 이런거 넣기
    // 유저 랭킹 정보를 레디스에 저장
    // 점수가 같으면 먼저 들어간 사람이 높은 등수!
    // value에 시간 값을 넣기->timestamp같은거 아무거나 currenttimemilli?->이거ㄱㄱ 1-시간 하면될듯
    public void saveRankMongoToRedis(RankMongo rankMongo) {
        String key="userRank";
        String userCode=rankMongo.getUserCode();
        double rating=rankMongo.getRating() == null ? 0.0 : rankMongo.getRating().doubleValue();
        redisTemplate.opsForZSet().add(key,userCode,rating);
    }
    
    // 랭겜 끝나면 점수를 레디스에 넣는 메소드
    // 게스트인지 확인하고 saveRankMongoToRedis 호출
    public void saveToRedis(UserProfile userProfile){
        if(!userProfile.isGuest()){
            Optional<RankMongo> rankMongoOptional=rankRepository.findById(userProfile.getUserCode());
            if(rankMongoOptional.isPresent()){
                RankMongo rankMongo=rankMongoOptional.get();
                saveRankMongoToRedis(rankMongo);
            }
        }
    }
    

    public String tierCalculation(int rank, int rating){
        if(rank<2){
            return "theorigin";
        }else if(rank<=5){
            return "rgb";
        }else if(rank<=10){
            return "colored";
        }else{
            if(rating<2500){
                return "nocolored";
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
    
}
