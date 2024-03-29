package com.ssafy.backend.rank.util;

import com.ssafy.backend.rank.dao.RankMongo;
import com.ssafy.backend.rank.repository.RankRepository;
import com.ssafy.backend.user.dto.UserProfileDto;
import com.ssafy.backend.user.entity.UserProfile;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
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
    public static String tierCalculation(int rank, int rating, long exp){
        // 경험치가 0이면=한판도 안했으면 nocolored
        // rank=-1-> 랭킹에 없는 경우니까 이떄도 nocolored
        if(exp==0||rank==-1){
            return "nocolored";
        }
        if(rank>0&&rank<2){
            return "origin";
        }else if(rank<=6){
            return "rgb";
        }else if(rank<=26){
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

    // 순위, 티어 세팅해주는 코드
    public void getMyRank(UserProfileDto userProfileDto){
        String userCode=userProfileDto.getUserCode();
        // 레디스에서 사용자의 점수(rating)와 등수(rank) 조회
        String key = "userRank";
        try {
            Double rating = redisTemplate.opsForZSet().score(key, userCode);
            Long rank = redisTemplate.opsForZSet().reverseRank(key, userCode);

            // 점수가 null인 경우 사용자가 랭킹에 없다고 가정하고 초기값 설정
            int userRating = rating != null ? rating.intValue() : 0;
            // 등수는 0부터 시작하므로 실제 등수를 얻기 위해 +1. 랭킹에 없는 경우 -1로 설정
            int userRank = rank != null ? rank.intValue() + 1 : -1;

            // userProfileDto 갱신(순위, 랭킹점수, 티어)
            userProfileDto.setRank(userRank);
            // 경험치로 못해서 레벨로 처리함
            userProfileDto.setTier(tierCalculation(userRank, userRating, userProfileDto.getLevel()));
        } catch (Exception e) {
            // Redis 접근 실패 또는 기타 예외 처리. 적절한 예외 처리 로직 추가
            System.err.println("랭킹 정보 조회 중 오류 발생: " + e.getMessage());
        }
    }

    // 서버 시작하면 mongo에 있는 usercode, rating을 userRank로 보낸다
    @EventListener(ApplicationReadyEvent.class)
    public void mongoToRedis(){
        // RankRepository를 사용하여 MongoDB에서 모든 RankMongo 객체를 조회
        List<RankMongo> rankList = rankRepository.findAll();

        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
        String key = "userRank";

        for (RankMongo rankMongo : rankList) {
            String userCode = rankMongo.getUserCode();
            Integer rating = rankMongo.getRating();

            // 사용자의 rating이 null이 아닌 경우에만 Redis에 저장
            if (rating != null) {
                // MongoDB에서 조회한 사용자 정보를 Redis의 userRank Sorted Set에 저장
                zSetOperations.add(key, userCode, rating.doubleValue());
            }
        }

        // 현재 시간을 "yyyy-MM-dd HH:mm:ss" 형식으로 포매팅
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedNow = now.format(formatter);

        System.out.println("서버 시작 후 MongoDB->Redis 데이터 이동 완료. 완료 시간: " + formattedNow);
    }

    // 12시간마다 mongo에 있는 데이터를 userRank로 보낸다
    @Scheduled(fixedRate = 43200000)
    public void updateMongoToRedis(){
        List<RankMongo> rankList = rankRepository.findAll();
        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
        String key = "userRank";

        // Redis의 userRank를 업데이트하기 전에 기존 데이터를 삭제할 수도 있습니다.
        // 이렇게 하면 항상 최신 상태의 데이터만 유지됩니다.
        // redisTemplate.delete(key);

        for (RankMongo rankMongo : rankList) {
            String userCode = rankMongo.getUserCode();
            Integer rating = rankMongo.getRating();

            // 사용자의 rating이 null이 아닌 경우에만 Redis에 저장
            if (rating != null) {
                // MongoDB에서 조회한 사용자 정보를 Redis의 userRank Sorted Set에 저장
                zSetOperations.add(key, userCode, rating.doubleValue());
            }
        }

        // 현재 시간을 "yyyy-MM-dd HH:mm:ss" 형식으로 포매팅
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedNow = now.format(formatter);

        System.out.println("정기적 MongoDB->Redis 업데이트 완료. 업데이트 시간: " + formattedNow);
    }




}
