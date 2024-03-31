package com.ssafy.backend.game.domain;

import com.ssafy.backend.game.type.EffectType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Effect {
    EffectType effectType;
    float x;
    float y;
}
