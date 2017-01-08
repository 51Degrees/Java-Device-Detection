package fiftyone.mobile.detection.helper;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fiftyone.mobile.Filename;
import fiftyone.mobile.detection.DatasetBuilder;
import fiftyone.mobile.detection.cache.ICache;
import fiftyone.mobile.detection.cache.ILoadingCache;
import fiftyone.mobile.detection.cache.IPutCache;
import fiftyone.mobile.detection.cache.IValueLoader;
import fiftyone.mobile.detection.StreamDataset;

import java.io.IOException;
import java.util.Date;

import static fiftyone.mobile.detection.DatasetBuilder.CacheType.*;
import static fiftyone.mobile.detection.DatasetBuilder.*;

/**
 * Example user supplied class providing a Guava Cache
 */
public class GuavaCache {

     static class CacheAdaptor <K,V>  implements ICache<K,V> {
        final Cache<K,V> cache;

        CacheAdaptor(Cache<K, V> cache) {
            this.cache = cache;
        }

        @Override
        public V get(K key) {
            return cache.getIfPresent(key);
        }

        @Override
        public long getCacheSize() {
            return cache.size();
        }

        @Override
        public long getCacheMisses() {
            return cache.stats().missCount();
        }

        @Override
        public long getCacheRequests() {
            return cache.stats().requestCount();
        }

        @Override
        public double getPercentageMisses() {
            return getCacheMisses()/getCacheRequests();
        }

        @Override
        public void resetCache() {
            cache.invalidateAll();
        }
    }

    static class PutCacheAdaptor<K,V> extends CacheAdaptor<K,V> implements IPutCache<K,V>{

        PutCacheAdaptor(Cache<K, V> cache) {
            super(cache);
        }

        @Override
        public void put(K key, V value) {
            cache.put(key, value);
        }
    }

    static class UaCacheAdaptor <K,V> extends PutCacheAdaptor<K,V> implements ILoadingCache<K,V> {

        UaCacheAdaptor(com.google.common.cache.Cache<K,V> cache) {
            super(cache);
        }

        @Override
        public V get(K key, IValueLoader<K, V> loader) throws IOException {
            V result = get(key);
            if (result == null) {
                result = loader.load(key);
                if (result != null) {
                    cache.put(key, result);
                }
            }
            return result;
        }

        @Override
        public V get(K key) {
            return cache.getIfPresent(key);
        }
    }

    public static StreamDataset getDatasetWithGuavaCaches() throws IOException {
        com.google.common.cache.Cache nodeCache = CacheBuilder.newBuilder()
                .initialCapacity(NODES_CACHE_SIZE)
                .maximumSize(NODES_CACHE_SIZE)
                .recordStats()
                .build();

        com.google.common.cache.Cache profileCache = CacheBuilder.newBuilder()
                .initialCapacity(PROFILES_CACHE_SIZE)
                .maximumSize(PROFILES_CACHE_SIZE)
                .recordStats()
                .build();

        com.google.common.cache.Cache stringsCache = CacheBuilder.newBuilder()
                .initialCapacity(STRINGS_CACHE_SIZE)
                .maximumSize(STRINGS_CACHE_SIZE)
                .recordStats()
                .build();

        com.google.common.cache.Cache valuesCache = CacheBuilder.newBuilder()
                .initialCapacity(VALUES_CACHE_SIZE)
                .maximumSize(VALUES_CACHE_SIZE)
                .recordStats()
                .build();

        com.google.common.cache.Cache signaturesCache = CacheBuilder.newBuilder()
                .initialCapacity(SIGNATURES_CACHE_SIZE)
                .maximumSize(SIGNATURES_CACHE_SIZE)
                .recordStats()
                .build();

        @SuppressWarnings("unchecked")
        StreamDataset dataset =
                DatasetBuilder.stream()
                        .addCache(NodesCache, new PutCacheAdaptor(nodeCache))
                        .addCache(ProfilesCache, new PutCacheAdaptor(profileCache))
                        .addCache(StringsCache, new PutCacheAdaptor(stringsCache))
                        .addCache(ValuesCache, new PutCacheAdaptor(valuesCache))
                        .addCache(SignaturesCache, new PutCacheAdaptor(signaturesCache))
                        .lastModified(new Date())
                        .build(Filename.LITE_PATTERN_V32);

        return dataset;
    }

    public static <K,V> ILoadingCache<K,V> getUserAgentCache() {
        com.google.common.cache.Cache<K,V> uaCache = CacheBuilder.newBuilder()
                .initialCapacity(20)
                .maximumSize(20)
                .recordStats()
                .build();

        return new UaCacheAdaptor<K,V>(uaCache);
    }
}
