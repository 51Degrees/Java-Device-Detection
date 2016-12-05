package fiftyone.device.proto;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.Modes;

import java.io.IOException;
import java.util.Date;

/**
 * @author jo
 */
public interface Cache <K,V>  {
    // returns null if absent
    V get(K key);
    void put(K key, V value);

    class Jsr107Adaptor <K,V> implements Cache<K,V>{
        private final javax.cache.Cache<K,V> jsr107;

        public Jsr107Adaptor(javax.cache.Cache jsr107) {
            //noinspection unchecked
            this.jsr107 = jsr107;
        }

        @Override
        public V get(K key) {
            return jsr107.get(key);
        }

        @Override
        public void put(K key, V value) {
            jsr107.put(key, value);
        }
    }

    class GuavaAdaptor <K,V> implements Cache<K,V>{
        private final com.google.common.cache.Cache<K,V> guavaCache;

        public GuavaAdaptor(com.google.common.cache.Cache guavaCache) {
            //noinspection unchecked
            this.guavaCache = guavaCache;
        }

        @Override
        public V get(K key) {
            return guavaCache.getIfPresent(key);
        }

        @Override
        public void put(K key, V value) {
            guavaCache.put(key, value);
        }
    }
}
