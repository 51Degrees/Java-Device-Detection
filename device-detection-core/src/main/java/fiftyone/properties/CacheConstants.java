package fiftyone.properties;

/**
 * Constants for caches for Stream {@link fiftyone.mobile.detection.entities.stream.Dataset}s
 */
public class CacheConstants {

    /* Default Cache sizes */

    public static final int STRINGS_CACHE_SIZE = 5000;
    public static final int NODES_CACHE_SIZE = 15000;
    public static final int VALUES_CACHE_SIZE = 5000;
    public static final int PROFILES_CACHE_SIZE = 600;
    public static final int SIGNATURES_CACHE_SIZE = 500;

    /**
     * Cache types for Stream Dataset
     */
    public enum CacheType {
        StringsCache, NodesCache, ValuesCache, ProfilesCache, SignaturesCache
    }
}
