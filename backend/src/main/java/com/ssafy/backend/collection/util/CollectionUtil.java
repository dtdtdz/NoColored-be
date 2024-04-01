package com.ssafy.backend.collection.util;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class CollectionUtil {

    // 승패 계산
    // 1등아니면 다 패배로 처리
    public String winLostCount(int playNumber, int rank) {
        if(playNumber==2){
            if(rank==1){
                return "Win";
            }else{
                return "Lose";
            }
        }else if(playNumber==3){
            if(rank==1){
                return "Win";
            }else{
                return "Lose";
            }
        }else{
            if(rank==1){
                return "Win";
            }else{
                return "Lose";
            }
        }
    }

    // 플탐은 계산 안하고 gameinfo에서 가져오기
    // 플레이타임 계산
    public long calculatePlaytimeInMinutes(LocalDateTime startTime, LocalDateTime endTime) {
        Duration duration = Duration.between(startTime, endTime);
        return duration.toMinutes();
    }

    // 같은 점수일때

}
