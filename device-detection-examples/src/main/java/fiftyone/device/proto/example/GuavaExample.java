package fiftyone.device.proto.example;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.cache.IPutCache;
import fiftyone.mobile.detection.cache.IUaMatchCache;
import fiftyone.mobile.detection.cache.IValueLoader;
import fiftyone.mobile.detection.factories.StreamFactory;

import java.io.IOException;
import java.util.Date;

/**
 * @author jo
 */
class GuavaExample {

    public static class CacheAdaptor <K,V> extends IPutCache.Base<K,V> implements IPutCache<K,V> {
        private final Cache<K,V> cache;

        public CacheAdaptor(com.google.common.cache.Cache<K,V> cache) {
            this.cache = cache;
        }

        @Override
        public V get(K key) {
            return cache.getIfPresent(key);
        }

        @Override
        public void put(K key, V value) {
            cache.put(key, value);
        }
    }

    public static class UaCacheAdaptor <K,V> extends IUaMatchCache.Base<K,V> implements IUaMatchCache<K,V> {

        private final Cache<K, V> cache;

        public UaCacheAdaptor(com.google.common.cache.Cache<K,V> cache) {
            this.cache = cache;
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

    public static void main (String[] args) throws IOException {
        com.google.common.cache.Cache uaCache = CacheBuilder.newBuilder()
                .initialCapacity(100000)
                .maximumSize(100000)
                .concurrencyLevel(5) // set to number of threads that can access cache at same time
                .build();

        com.google.common.cache.Cache nodeCache = CacheBuilder.newBuilder()
                .initialCapacity(StreamFactory.NODES_CACHE_SIZE)
                .maximumSize(StreamFactory.NODES_CACHE_SIZE)
                .concurrencyLevel(5)
                .build();

        com.google.common.cache.Cache profileCache = CacheBuilder.newBuilder()
                .initialCapacity(StreamFactory.PROFILES_CACHE_SIZE)
                .maximumSize(StreamFactory.PROFILES_CACHE_SIZE)
                .concurrencyLevel(5)
                .build();

        Dataset dataset = new StreamFactory.Builder()
                .addCache(StreamFactory.CacheType.NodesCache, new CacheAdaptor(nodeCache))
                .addCache(StreamFactory.CacheType.ProfilesCache, new CacheAdaptor(profileCache))
                .isTempfile()
                .lastModified(new Date())
                .build("data/51Degrees-LiteV3.2.dat");

        Provider provider = new Provider(dataset, new UaCacheAdaptor(uaCache));

        Match match = provider.match("Hello World");
        System.out.printf("%s", match.getSignature());
    }
}
