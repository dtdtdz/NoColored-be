package com.ssafy.backend.websocket.domain;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum SendBinaryMessageType {

    START((byte) 0),
    SET_CHARACTER((byte) 1),
    TIME((byte) 2),
    COUNT_DOWN((byte) 3),
    ITEM((byte) 10), //event
    END((byte) 20),
    PHYSICS_STATE((byte) 100),
    SCORE((byte) 101),
    EFFECT((byte) 102),
    SKIN((byte) 110),
//    ITEM_USE((byte) 110), //event
    TEST_MAP((byte) 200);

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
