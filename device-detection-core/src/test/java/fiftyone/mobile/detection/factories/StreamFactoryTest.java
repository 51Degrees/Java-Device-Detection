package fiftyone.mobile.detection.factories;

import com.google.common.io.Files;
import fiftyone.mobile.Filename;
import fiftyone.mobile.StandardUnitTest;
import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.IReadonlyList;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.cache.ICache;
import fiftyone.mobile.detection.cache.LruCache;
import fiftyone.mobile.detection.helper.GuavaCache;
import fiftyone.mobile.detection.helper.ViableProvider;
import fiftyone.properties.CacheConstants;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import static fiftyone.mobile.detection.helper.GuavaCache.getDatasetWithGuavaCaches;
import static fiftyone.properties.CacheConstants.CacheType.*;
import static org.junit.Assert.*;

/**
 * Validate StreamFactory
 */
public class StreamFactoryTest extends StandardUnitTest {

    // create a 3.2 pattern provider using a builder
    @Test
    public void testCreate32FromBuilder () throws Exception {
        File temp = File.createTempFile("Test",".dat");
        File source = new File(Filename.LITE_PATTERN_V32);
        Files.copy(source, temp);

        Dataset dataset = new StreamFactory.Builder()
                .lastModified(new Date())
                .build(temp.getPath());
        ViableProvider.ensureViableProvider(new Provider(dataset));
        dataset.close();
        // temp must still exist after close
        assertTrue(temp.exists());

        // assess whether temporary file gets deleted
        dataset = new StreamFactory.Builder()
                .isTempfile()
                .lastModified(new Date())
                .build(temp.getPath());

        ViableProvider.ensureViableProvider(new Provider(dataset));
        dataset.close();
        // temp must NOT still exist after close
        assertFalse(temp.exists());

    }

    // create a 3.2 pattern provider using factory create method
    @Test
    public void testCreate32FromFilename () throws Exception {
        File temp = File.createTempFile("Test",".dat");
        File source = new File(Filename.LITE_PATTERN_V32);
        Files.copy(source, temp);

        Dataset dataset = StreamFactory.create(temp.getPath());
        ViableProvider.ensureViableProvider(new Provider(dataset));
        dataset.close();
        // temp must still exist after close
        assertTrue(temp.exists());

        // assess whether temporary file gets deleted
        dataset = StreamFactory.create(temp.getPath(), true);
        ViableProvider.ensureViableProvider(new Provider(dataset));
        dataset.close();
        // temp must NOT still exist after close
        assertFalse(temp.exists());

    }

    // create 3.1 pattern provider using factory create method
    @Test
    public void testCreate31FromFilename () throws Exception {
        Dataset dataset = StreamFactory.create(Filename.LITE_PATTERN_V31);
        ViableProvider.ensureViableProvider(new Provider(dataset));
    }

    // tests to see if Stream and Memory load the same thing (default caches)
    @Test
    public void testMemoryStreamDatasetConsistentDefault () throws IOException {

        fiftyone.mobile.detection.entities.stream.Dataset streamDataset =
                StreamFactory.create(Filename.LITE_PATTERN_V32);
        Dataset memoryDataset = MemoryFactory.create(Filename.LITE_PATTERN_V32);

        compareDatasets(streamDataset, memoryDataset);
    }

    // tests to see if Stream and Memory load the same thing (no caches)
    @Test
    public void testMemoryStreamDatasetConsistentNoCache () throws IOException {

        fiftyone.mobile.detection.entities.stream.Dataset streamDataset = new StreamFactory.Builder()
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

        fiftyone.mobile.detection.entities.stream.Dataset streamDataset = new StreamFactory.Builder()
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

        fiftyone.mobile.detection.entities.stream.Dataset streamDataset = getDatasetWithGuavaCaches();
        Dataset memoryDataset = MemoryFactory.create(Filename.LITE_PATTERN_V32);

        compareDatasets(streamDataset, memoryDataset);
    }

    // see if the cache metrics etc work when using default cache.
    @Test
    public void testDefaultCache () throws Exception {
        fiftyone.mobile.detection.entities.stream.Dataset dataset = StreamFactory.create(Filename.LITE_PATTERN_V32);
        Provider provider = new Provider(dataset, 20);

        cacheTests(provider);
    }

    // see if cache metrics etc work with Guava cache
    @Test
    public void testGuavaCache () throws Exception {
        fiftyone.mobile.detection.entities.stream.Dataset dataset = getDatasetWithGuavaCaches();
        Provider provider = new Provider(dataset, GuavaCache.getUserAgentCache());

        cacheTests(provider);
    }


    // --- helpers

    private void compareDatasets(fiftyone.mobile.detection.entities.stream.Dataset streamDataset, Dataset memoryDataset) {
        logger.debug("Strings");
        compareStreamMemory(streamDataset.strings, memoryDataset.strings);
        printDatasetCacheInfo(streamDataset);

        logger.debug("Signatures");
        compareStreamMemory(streamDataset.signatures, memoryDataset.signatures);
        printDatasetCacheInfo(streamDataset);

        logger.debug("Profiles");
        compareStreamMemory(streamDataset.profiles, memoryDataset.profiles);
        printDatasetCacheInfo(streamDataset);

        logger.debug("Nodes");
        compareStreamMemory(streamDataset.nodes, memoryDataset.nodes);
        printDatasetCacheInfo(streamDataset);

        logger.debug("Values");
        compareStreamMemory(streamDataset.values, memoryDataset.values);
        printDatasetCacheInfo(streamDataset);
    }



    private void cacheTests(Provider provider) throws IOException {
        ViableProvider.ensureViableProvider(provider);
        fiftyone.mobile.detection.entities.stream.Dataset dataset = (fiftyone.mobile.detection.entities.stream.Dataset) provider.dataSet;
        assertEquals(1, provider.getCacheMisses());
        assertEquals(1, provider.getCacheRequests(), 0);
        printDatasetCacheInfo(dataset);

        long stringsRequests = dataset.getCache(StringsCache).getCacheRequests();
        long stringsMisses = dataset.getCache(StringsCache).getCacheMisses();
        long nodesRequests = dataset.getCache(NodesCache).getCacheRequests();
        long nodesMisses = dataset.getCache(NodesCache).getCacheMisses();
        long valuesRequests = dataset.getCache(ValuesCache).getCacheRequests();
        long valuesMisses = dataset.getCache(ValuesCache).getCacheMisses();
        long profilesRequests = dataset.getCache(ProfilesCache).getCacheRequests();
        long profilesMisses = dataset.getCache(ProfilesCache).getCacheMisses();
        long signaturesRequests = dataset.getCache(SignaturesCache).getCacheRequests();
        long signaturesMisses = dataset.getCache(SignaturesCache).getCacheMisses();


        // check that the requests go up and that nothing hits the backend caches
        ViableProvider.ensureViableProvider(provider);
        assertEquals(1, provider.getCacheMisses());
        assertEquals(2, provider.getCacheRequests(), 0);
        assertEquals(stringsRequests, dataset.getCache(StringsCache).getCacheRequests());
        assertEquals(stringsMisses, dataset.getCache(StringsCache).getCacheMisses());
        assertEquals(nodesRequests, dataset.getCache(NodesCache).getCacheRequests());
        assertEquals(nodesMisses, dataset.getCache(NodesCache).getCacheMisses());
        assertEquals(valuesRequests, dataset.getCache(ValuesCache).getCacheRequests());
        assertEquals(valuesMisses, dataset.getCache(ValuesCache).getCacheMisses());
        assertEquals(profilesRequests, dataset.getCache(ProfilesCache).getCacheRequests());
        assertEquals(profilesMisses, dataset.getCache(ProfilesCache).getCacheMisses());
        assertEquals(signaturesRequests, dataset.getCache(SignaturesCache).getCacheRequests());
        assertEquals(signaturesMisses, dataset.getCache(SignaturesCache).getCacheMisses());

        // again check that the requests go up and that nothing hits the backend caches
        ViableProvider.ensureViableProvider(provider);
        assertEquals(1, provider.getCacheMisses());
        assertEquals(3, provider.getCacheRequests(), 0);
        assertEquals(stringsRequests, dataset.getCache(StringsCache).getCacheRequests());
        assertEquals(stringsMisses, dataset.getCache(StringsCache).getCacheMisses());
        assertEquals(nodesRequests, dataset.getCache(NodesCache).getCacheRequests());
        assertEquals(nodesMisses, dataset.getCache(NodesCache).getCacheMisses());
        assertEquals(valuesRequests, dataset.getCache(ValuesCache).getCacheRequests());
        assertEquals(valuesMisses, dataset.getCache(ValuesCache).getCacheMisses());
        assertEquals(profilesRequests, dataset.getCache(ProfilesCache).getCacheRequests());
        assertEquals(profilesMisses, dataset.getCache(ProfilesCache).getCacheMisses());
        assertEquals(signaturesRequests, dataset.getCache(SignaturesCache).getCacheRequests());
        assertEquals(signaturesMisses, dataset.getCache(SignaturesCache).getCacheMisses());
    }

    private void printDatasetCacheInfo(fiftyone.mobile.detection.entities.stream.Dataset dataset) {
        for (CacheConstants.CacheType type: CacheConstants.CacheType.values()) {
            ICache cache = dataset.getCache(type);
            if (cache == null) {
                logger.debug(type + " is null");
            } else {
                logger.debug("Cache {}, Misses: {}, Requests: {}", type,
                        cache.getCacheMisses(),
                        cache.getCacheRequests());
            }
        }
    }

    private void compareStreamMemory(IReadonlyList stream, IReadonlyList memory) {
        assertEquals(stream.size(), memory.size());
        Iterator streamIt = stream.iterator();
        Iterator memoryIt = stream.iterator();
        while (streamIt.hasNext()) {
            assertEquals(streamIt.next().toString(), memoryIt.next().toString());
        }
        assertFalse(memoryIt.hasNext());
    }
}