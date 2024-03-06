package com.ssafy.backend.assets;

import java.util.HashMap;
import java.util.Map;

public class TwoWayMap<K, V> {
    private Map<K, V> keyToValueMap = new HashMap<>();
    private Map<V, K> valueToKeyMap = new HashMap<>();

    public void put(K key, V value) {
        keyToValueMap.put(key, value);
        valueToKeyMap.put(value, key);
    }

    public V getValue(K key) {
        return keyToValueMap.get(key);
    }

    public K getKey(V value) {
        return valueToKeyMap.get(value);
    }

    public void removeByKey(K key) {
        V value = keyToValueMap.remove(key);
        valueToKeyMap.remove(value);
    }

    public void removeByValue(V value) {
        K key = valueToKeyMap.remove(value);
        keyToValueMap.remove(key);
    }
}
