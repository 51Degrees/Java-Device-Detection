/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fiftyone.mobile.detection;

import java.util.Map;

/**
 *
 * @param <K>
 * @param <V>
 */
public class NameValue<K, V> implements Map.Entry<K,V> {
    /**
     * The key of this pair.
     */
    private final K key;
    /**
     * The value of this pair.
     */
    private V value;
    
    /**
     * Constructs a new Key Value pair.
     * @param key
     * @param value 
     */
    public NameValue(K key, V value) {
        this.key = key;
        this.value = value;
    }
    
    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        V old = this.value;
        this.value = value;
        return old;
    }
}
