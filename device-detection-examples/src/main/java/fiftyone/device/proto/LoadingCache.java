package fiftyone.device.proto;

/**
 * The internal 51D cache implementations will be these ... i.e. the wrap the passed
 * incoming caches - and wrap Entirty factories to make loaders
 * @author jo
 */
class LoadingCache <Integer, V>  {
    private final CacheLoader<Integer, V> loader;
    private Cache<Integer, V> cache;

    public LoadingCache (CacheLoader<Integer, V> loader) {
        this.loader = loader;
    }

    void setCache(Cache<Integer, V> cache) {
        this.cache = cache;
    }

    V get(Integer key) {
        V v = cache.get(key);
        if (v == null) {
            v = loader.load(key);
            if (v != null) {
                cache.put(key, v);
            }
        }
        return v;
    }

}
