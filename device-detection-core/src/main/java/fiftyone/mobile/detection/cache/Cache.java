package fiftyone.mobile.detection.cache;

/**
 * Interface for a loading cache
 */
public interface Cache <K,V> {
    /**
     * Instances allow the cache to load an entry on cache miss
     * @param <K>  Key
     * @param <V>  Value
     */
    interface Loader <K,V> {
        /**
         * Load a Cache entry using the key
         * @param key a key
         * @return a new cache entry or null if failed or not present
         */
        V load (K key);

        /**
         * load a cache entry using the key and populate a result instance the while
         * @param key a key
         * @param resultInstance an instance to be populated
         * @return a cache entry or null if none found
         */
        V load (K key, V resultInstance);
    }

    /**
     * Empty the cache and reset stats
     */
    void resetCache();

    /**
     * Clear the cache request count
     */
    void clearRequests();

    /**
     * Clear the cache miss count
     */
    void clearMisses();

    /**
     * Set a default loader for this cache
     * @param loader  the loader to use
     */
    void setLoader(Loader<K, V> loader);

    /**
     * Get cache retrieval count
     * @return the retrieval count
     */
    long getCacheRequests();

    /**
     * Get the number of cache misses
     * @return the count
     */
    long getCacheMisses();

    /**
     * Enable or disable stats collection
     * @param enable true to enable
     * @return the previous value
     */
    @SuppressWarnings("unused")
    boolean enableStats(boolean enable);

    /**
     * Get an entry from the cache, and try to load it if absent
     * @param key the key to find
     * @return a value or null if none found
     */
    V get(K key);

    /**
     * Get an entry from the cache, and try to load it if absent
     * @param key the key to find
     * @param resultInstance an instance for implementations to use and populate
     * @return a value or null if none found
     */
    V get(K key, V resultInstance);

    /**
     * get a value from the cache but do not load if absent
     * @param key the key to find
     * @return a value or null if not found
     */
    V getIfCached(K key);

    /**
     * get a value from the cache and load if absent using the supplied loader
     * @param key a key
     * @param loader a loader
     * @return a value or null if no value found
     */
    V get(K key, Loader<K,V> loader);
}
