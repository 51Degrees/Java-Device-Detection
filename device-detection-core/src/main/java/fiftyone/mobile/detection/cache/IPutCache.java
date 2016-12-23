package fiftyone.mobile.detection.cache;

/**
 * A cache that supports a thread safe put method for inserting to cache.
 * <p>
 * By contrast, for example, {@link LruCache} is a loading cache, it automaticlly
 * updates itelf by being provided with a data loader.
 */
public interface IPutCache<K,V> extends ICache<K,V> {
    void put(K key, V value);
}
