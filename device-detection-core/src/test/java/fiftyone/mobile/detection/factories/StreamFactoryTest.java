package fiftyone.mobile.detection.factories;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fiftyone.mobile.StandardUnitTest;
import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.Filename;
import fiftyone.mobile.detection.IReadonlyList;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.cache.IPutCache;
import fiftyone.mobile.detection.cache.IUaMatchCache;
import fiftyone.mobile.detection.cache.IValueLoader;
import fiftyone.mobile.TestType;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * @author jo
 */
public class StreamFactoryTest extends StandardUnitTest {

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

        Dataset streamDataset = new StreamFactory.Builder()
/*
                .addCache(StreamFactory.CacheType.NodesCache, new CacheAdaptor(nodeCache))
                .addCache(StreamFactory.CacheType.ProfilesCache, new CacheAdaptor(profileCache))
*/
                .isTempfile()
                .lastModified(new Date())
                .build(Filename.LITE_PATTERN_V32);


        Dataset memoryDataset = MemoryFactory.create(Filename.LITE_PATTERN_V32);

        compareStreamMemory(streamDataset.strings, memoryDataset.strings);
        compareStreamMemory(streamDataset.signatures, memoryDataset.signatures);
        compareStreamMemory(streamDataset.profiles, memoryDataset.profiles);
        compareStreamMemory(streamDataset.nodes, memoryDataset.nodes);
        compareStreamMemory(streamDataset.values, memoryDataset.values);

        Common.ensureViableProvider(new Provider(memoryDataset));
        Common.ensureViableProvider(new Provider(streamDataset));
    }

    private void compareStreamMemory(IReadonlyList stream, IReadonlyList memory) {
        assertEquals(stream.size(), memory.size());
        Iterator streamIt = stream.iterator();
        Iterator memoryIt = stream.iterator();
        for (int i=0; i < 20; i++) {
            System.out.println(streamIt.next());
            System.out.println(memoryIt.next());
            assertEquals(streamIt.next().toString(), memoryIt.next().toString());
        }
    }

}