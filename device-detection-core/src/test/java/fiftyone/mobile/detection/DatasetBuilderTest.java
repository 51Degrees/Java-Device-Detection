package fiftyone.mobile.detection;

import com.google.common.io.Files;
import fiftyone.mobile.Filename;
import fiftyone.mobile.StandardUnitTest;
import fiftyone.mobile.detection.cache.LruCache;
import fiftyone.mobile.detection.entities.stream.StreamDataset;
import fiftyone.mobile.detection.factories.MemoryFactory;
import fiftyone.mobile.detection.helper.GuavaCache;
import fiftyone.mobile.detection.helper.ViableProvider;
import org.junit.Test;

import java.io.*;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static fiftyone.mobile.detection.DatasetBuilder.CacheType.*;
import static fiftyone.mobile.detection.helper.DatasetHelper.cacheTests;
import static fiftyone.mobile.detection.helper.DatasetHelper.compareDatasets;
import static fiftyone.mobile.detection.helper.GuavaCache.getDatasetWithGuavaCaches;
import static org.junit.Assert.*;

/**
 * @see fiftyone.mobile.detection.factories.StreamFactoryTest which tests some aspects implicitly
 * as StreamFactory depends on DatasetBuilder
 */
public class DatasetBuilderTest extends StandardUnitTest {
    // default is to have no caches
    @Test
    public void testDefaultIsNoCaches () throws IOException {
        StreamDataset dataset = DatasetBuilder.stream()
                .build(Filename.LITE_PATTERN_V32);
        assertEquals(null, dataset.getCache(StringsCache));
        assertEquals(null, dataset.getCache(SignaturesCache));
        assertEquals(null, dataset.getCache(NodesCache));
        assertEquals(null, dataset.getCache(ValuesCache));
        assertEquals(null, dataset.getCache(ProfilesCache));
    }

    // check that the default caches are added
    @Test
    public void testAddDefaultCaches () throws IOException {
        StreamDataset dataset = DatasetBuilder.stream()
                .addDefaultCaches()
                .build(Filename.LITE_PATTERN_V32);
        assertEquals(LruCache.class, dataset.getCache(StringsCache).getClass());
        assertEquals(LruCache.class, dataset.getCache(SignaturesCache).getClass());
        assertEquals(LruCache.class, dataset.getCache(NodesCache).getClass());
        assertEquals(LruCache.class, dataset.getCache(ValuesCache).getClass());
        assertEquals(LruCache.class, dataset.getCache(ProfilesCache).getClass());
    }

    // create a 3.2 pattern provider using a builder
    @Test
    public void testCreate32FromBuilder () throws Exception {
        File temp = File.createTempFile("Test",".dat");
        File source = new File(Filename.LITE_PATTERN_V32);
        Files.copy(source, temp);

        Dataset dataset = DatasetBuilder.stream()
                .lastModified(new Date())
                .build(temp.getPath());
        ViableProvider.ensureViableProvider(new Provider(dataset));
        dataset.close();
        // temp must still exist after close
        assertTrue(temp.exists());

        // assess whether temporary file gets deleted
        dataset = DatasetBuilder.stream()
                .setTempFile()
                .lastModified(new Date())
                .build(temp.getPath());

        ViableProvider.ensureViableProvider(new Provider(dataset));
        dataset.close();
        // temp must NOT still exist after close
        assertFalse(temp.exists());
    }

    @Test(expected = NoSuchElementException.class)
    public void testIterator () throws Exception {
        StreamDataset streamDataset = DatasetBuilder.stream()
                .lastModified(new Date())
                .build(Filename.LITE_PATTERN_V32);
        Iterator it = streamDataset.profiles.iterator();
        while (it.hasNext()) {
            it.next();
        }
        it.next();
    }

    // tests to see if Stream and Memory load the same thing (no caches)
    @Test
    public void testMemoryStreamDatasetConsistentNoCache () throws IOException {

        StreamDataset streamDataset = DatasetBuilder.stream()
                .lastModified(new Date())
                .build(Filename.LITE_PATTERN_V32);
        Dataset memoryDataset = MemoryFactory.create(Filename.LITE_PATTERN_V32);

        compareDatasets(streamDataset, memoryDataset);
    }

    // tests to see if Stream and Memory load the same thing (user supplied partial LRUCache)
    @Test
    public void testMemoryStreamDatasetConsistentPartialLruCache () throws IOException {
        LruCache nodesCache = new LruCache(20);
        LruCache valuesCache = new LruCache(100);
        LruCache stringsCache = new LruCache(100);

        StreamDataset streamDataset = DatasetBuilder.stream()
                .addCache(NodesCache, nodesCache)
                .addCache(ValuesCache, valuesCache)
                .addCache(StringsCache, stringsCache)
                .build(Filename.LITE_PATTERN_V32);
        Dataset memoryDataset = MemoryFactory.create(Filename.LITE_PATTERN_V32);

        compareDatasets(streamDataset, memoryDataset);
    }

    // see if stream and memory load same thing with a full set of Guava caches
    @Test
    public void testMemoryStreamDatasetConsistentGuava () throws Exception {

        StreamDataset streamDataset = getDatasetWithGuavaCaches();
        Dataset memoryDataset = MemoryFactory.create(Filename.LITE_PATTERN_V32);

        compareDatasets(streamDataset, memoryDataset);
    }

    // see if cache metrics etc work with Guava cache
    @Test
    public void testGuavaCache () throws Exception {
        Dataset dataset = getDatasetWithGuavaCaches();
        Provider provider = new Provider(dataset, GuavaCache.getUserAgentCache());

        cacheTests(provider);
    }
}
