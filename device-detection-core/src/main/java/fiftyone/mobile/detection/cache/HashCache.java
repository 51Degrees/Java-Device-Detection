package fiftyone.mobile.detection.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * A non thread safe Cache implementation
 */
@SuppressWarnings("unused")
public class HashCache <K,V> implements Cache <K,V> {
    Map<K,V> map = new HashMap<K, V>(50000);
    private Loader<K,V> loader;

    @Override
    public void resetCache() {

    }

    @Override
    public void clearRequests() {

    }

    @Override
    public void clearMisses() {

    }

    @Override
    public long getCacheRequests() {
        return 0;
    }

    @Override
    public long getCacheMisses() {
        return 0;
    }

    @Override
    public boolean enableStats(boolean enable) {
        return false;
    }

    @Override
    public V get(K key) {
        return get(key, loader);
    }

    @Override
    public V get(K key, V resultInstance) {
        return get(key, resultInstance, loader);
    }

    @Override
    public V getIfCached(K key) {
        return map.get(key);
    }

    @Override
    public V get(K key, Loader<K, V> loader) {
        V result = getIfCached(key);
        if (result == null) {
            result = loader.load(key);
            map.put(key, result);
        }
        return result;
    }

    public V get(K key, V resultInstance, Loader<K, V> loader) {
        V result = getIfCached(key);
        if (result == null) {
            result = loader.load(key, resultInstance);
            map.put(key, result);
        }
        return result;
    }

    @Override
    public void setLoader(Loader<K, V> loader) {
        this.loader = loader;
    }
}
