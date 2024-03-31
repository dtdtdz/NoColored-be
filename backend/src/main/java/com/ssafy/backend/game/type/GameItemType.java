package com.ssafy.backend.game.type;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum GameItemType {
    NO_ITEM((byte) 0),
    LIGHT_U_PALL((byte) 1),
    STOP_NPC((byte) 2),
    RANDOM_BOX((byte) 3),
    REBEL((byte) 4),
    STOP_PLAYER((byte) 5);
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

    public static int size(){
        return map.size();
    }

}
