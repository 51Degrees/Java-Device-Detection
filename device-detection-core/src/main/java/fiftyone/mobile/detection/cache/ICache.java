package fiftyone.mobile.detection.cache;

/**
 * Interface for caches as used internally to the system
 * <p>
 * Aside from {@link #get(Object)} methods are optional.
 * <p>
 * Implementations may choose to respond "-1" for
 * methods that return numbers and may throw {@link UnsupportedOperationException}
 * for {@link #resetCache()}.
 *
 * @author jo
 */
public interface ICache<K,V> {
    long getCacheSize();

    long getCacheMisses();

    long getCacheRequests();

    double getPercentageMisses();

    /**
     *
     * @param key not null key to retrieve value
     * @return null if value is not present
     * @throws IllegalStateException if misoperation of underlying mechanisms
     */
    V get(K key);

    void resetCache();

    abstract class Base <K, V> implements ICache<K, V> {
        @Override
        public long getCacheSize() {
            return 0;
        }

        @Override
        public long getCacheMisses() {
            return 0;
        }

        @Override
        public long getCacheRequests() {
            return 0;
        }

        @Override
        public double getPercentageMisses() {
            return 0;
        }

        @Override
        public void resetCache() {
            throw new IllegalStateException("Cache reset not supported");
        }
    }
}
