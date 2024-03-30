package com.ssafy.backend.game.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RewardDto {
    List<String> skins;
    TierDto tier;
}
