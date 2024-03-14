package com.ssafy.backend.game.domain;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum SendBinaryMessageType {

    START((byte) 0),
    SET_CHARACTER((byte) 1),
    PHYSICS_STATE((byte) 2),
    TIME((byte) 3),
    STEP((byte) 4),
    GAME_END((byte) 10),
    TEST_MAP((byte) 100);

    private final byte value;
    private static final Map<Byte, SendBinaryMessageType> map = new HashMap<>();

    SendBinaryMessageType(byte value) {
        this.value = value;
    }
    static {
        for (SendBinaryMessageType messageType : SendBinaryMessageType.values()) {
            map.put(messageType.value, messageType);
        }
    }
    public static SendBinaryMessageType valueOf(byte value) {
        SendBinaryMessageType result = map.get(value);
        if (result == null) {
//            throw new IllegalArgumentException("No enum constant for value: " + value);
        }
        return result;
    }

}
