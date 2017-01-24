package fiftyone.mobile.detection.cache;

/**
 * Provides a method to build caches implementing {@link ICache}
 */
public interface ICacheBuilder {
    /**
     * Build and return an {@link ICache}
     * @param size The maximum number of entries that will be stored in the cache
     * @return The cache
     */
    ICache build(int size);
}
