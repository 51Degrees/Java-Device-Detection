package fiftyone.mobile.detection.cache;

/**
 * Interface for caches as used internally to the system
 * <p>
 * Aside from {@link #get(Object)} methods are optional.
 * <p>
 * Implementations may choose to respond "-1" for
 * methods that return numbers and proiver a no op
 * for {@link #resetCache()}
 */
public interface ICache<K,V> {
    /**
     * The size of the cache as number of entries
     */
    long getCacheSize();

    /**
     * The number of requests that could not be served
     */
    long getCacheMisses();

    /**
     * The number of requests made to the cache
     */
    long getCacheRequests();

    /**
     * a fraction < 1 (not a percentage) misses/requests
     */
    double getPercentageMisses();

    /**
     *
     * @param key not null key to retrieve value
     * @return null if value is not present
     * @throws IllegalStateException if misoperation of underlying mechanisms
     */
    V get(K key);

    /**
     * Remove all entries from the cache and reset statistics if possible.
     */
    void resetCache();

    /**
     * Factory for ICaches
     */
    interface Builder {
       ICache build(int size);
    }

    abstract class Base <K, V> implements ICache<K, V> {
        @Override
        public long getCacheSize() {
            return -1;
        }

        @Override
        public long getCacheMisses() {
            return -1;
        }

        @Override
        public long getCacheRequests() {
            return -1;
        }

        @Override
        public double getPercentageMisses() {
            if (getCacheRequests() == 0) {
                return -1;
            }
            return getCacheMisses()/getCacheRequests();
        }

        @Override
        public void resetCache() {}
    }
}
