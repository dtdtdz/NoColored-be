package com.ssafy.backend.game.type;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum EffectType {
    SKIN_APPEAR((byte) 0),
    ITEM_STOP((byte) 1),
    REBEL((byte) 2),
    ITEM_TIME_OUT((byte) 3),
    ITEM_USE((byte) 4);


    private final byte value;
    private static final Map<Byte, EffectType> map = new HashMap<>();

    EffectType(byte value) {
        this.value = value;
    }
    static {
        for (EffectType effectType : EffectType.values()) {
            map.put(effectType.value, effectType);
        }
    }
    public static EffectType valueOf(byte value) {
        EffectType result = map.get(value);
        if (result == null) {
//            throw new IllegalArgumentException("No enum constant for value: " + value);
        }
        return result;
    }
}
