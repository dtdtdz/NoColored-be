package com.ssafy.backend.play.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RankingServiceImpl implements RankingService{
    @Override
    public boolean addMatchingList(String token) {

        return false;
    }

    @Override
    public boolean delMatchingList(String token) {
        return false;
    }

    @Scheduled(fixedRate = 1000)
    private void matching(){

    }
}
