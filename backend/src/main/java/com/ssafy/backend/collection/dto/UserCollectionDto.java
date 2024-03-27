package com.ssafy.backend.collection.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
public class UserCollectionDto {
    private List<SkinDto> skins;
    private List<TitleDto> labels;
    private List<AchievementDto> achievements;
}
