package com.ssafy.backend.collection.util;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class CollectionUtil {

    // 승패 계산
    // 1등아니면 다 패배로 처리
    // 누적 2인 누적 3인 이런거 해도될듯
    // 2명이서 하는 게임일때는 1등이 승리, 2등이 패배
    // 3명이서 하는 게임일때는 1등이 승리, 3등이 패배
    // 4명이서 하는 게임일때는 1등이 승리, 4등이 패배
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
            }else if(rank==3){
                return "Lose";
            }else{
                return "Draw";
            }
        }else{
            if(rank==1){
                return "Win";
            }else if(rank==4){
                return "Lose";
            }else{
                return "Draw";
            }
        }
    }

    // 플레이타임 계산
    public long calculatePlaytimeInMinutes(LocalDateTime startTime, LocalDateTime endTime) {
        Duration duration = Duration.between(startTime, endTime);
        return duration.toMinutes();
    }

    // 같은 점수일때

}
