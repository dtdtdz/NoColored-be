package com.ssafy.backend.game.domain;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum GameItemType {
    LIGHT_U_PALL((byte) 0),
    STOP_NPC((byte) 1),
    RANDOM_BOX((byte) 2),
    REBEL((byte) 3),
    STOP_PLAYER((byte) 4),
    COLOR_LESS((byte) 255);
//    INVINCIBLE,
//    NINJA,
//    FIREWORKS,
//    BLACKOUT,
//    LIGHT_U_PONCE,
//    AGAIN,
    private final byte value;
    private static final Map<Byte, GameItemType> map = new HashMap<>();

    GameItemType(byte value) {
        this.value = value;
    }
    static {
        for (GameItemType gameItemType : GameItemType.values()) {
            map.put(gameItemType.value, gameItemType);
        }
    }
    public static GameItemType valueOf(byte value) {
        GameItemType result = map.get(value);
        if (result == null) {
//            throw new IllegalArgumentException("No enum constant for value: " + value);
        }
        return result;
    }


}
