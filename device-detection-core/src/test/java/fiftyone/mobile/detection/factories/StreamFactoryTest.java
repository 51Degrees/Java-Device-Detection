package fiftyone.mobile.detection.factories;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.Filename;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.cache.IPutCache;
import fiftyone.mobile.detection.cache.IUaMatchCache;
import fiftyone.mobile.detection.cache.IValueLoader;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

/**
 * @author jo
 */
public class StreamFactoryTest {

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


    @Test
    public void testCreate() throws Exception {
        File testFile = new File(Filename.LITE_PATTERN_V31);
        FileInputStream fileInputStream = new FileInputStream(testFile);

        com.google.common.cache.Cache uaCache = CacheBuilder.newBuilder()
                .initialCapacity(1000)
                .maximumSize(100000)
                .concurrencyLevel(5)
                .build();

        com.google.common.cache.Cache nodeCache = CacheBuilder.newBuilder()
                .initialCapacity(StreamFactory.NODES_CACHE_SIZE)
                .maximumSize(StreamFactory.NODES_CACHE_SIZE)
                .build();

        com.google.common.cache.Cache profileCache = CacheBuilder.newBuilder()
                .initialCapacity(StreamFactory.PROFILES_CACHE_SIZE)
                .maximumSize(StreamFactory.PROFILES_CACHE_SIZE)
                .build();

        Dataset dataset = new StreamFactory.Builder()
                .addCache(StreamFactory.CacheType.NodesCache, new CacheAdaptor(nodeCache))
                .addCache(StreamFactory.CacheType.ProfilesCache, new CacheAdaptor(profileCache))
                .isTempfile()
                .lastModified(new Date())
                .build(Filename.LITE_PATTERN_V32);


        Iterator it = dataset.strings.iterator();
        for (int i=0; i < 20; i++) {
            System.out.println(it.next());
        }

        System.out.println(dataset.strings.size());

        it = dataset.profiles.iterator();
        for (int i=0; i < 20; i++) {
            System.out.println(it.next());
        }

        System.out.println(dataset.profiles.size());

        MemoryFactoryTest.ensureViableProvider(new Provider(dataset));
    }

}