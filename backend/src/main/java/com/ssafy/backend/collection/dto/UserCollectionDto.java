package com.ssafy.backend.collection.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
public class UserCollectionDto {
    private List<SkinDto> skins;
    private List<TitleDto> titles;
    private List<AchievementDto> achievements;
}
