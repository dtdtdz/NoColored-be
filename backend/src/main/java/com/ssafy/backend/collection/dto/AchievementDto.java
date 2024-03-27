package com.ssafy.backend.collection.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AchievementDto {
    private int id;
    private String name;
    private String reward;
    private boolean isAchieved;
}
