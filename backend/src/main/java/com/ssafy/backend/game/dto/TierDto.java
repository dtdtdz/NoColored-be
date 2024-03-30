package com.ssafy.backend.game.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TierDto {
    String oldtier;
    String newtier;
    boolean upgrade;
}
