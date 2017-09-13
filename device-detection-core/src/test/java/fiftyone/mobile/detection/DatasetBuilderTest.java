/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited.
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
 * Caversham, Reading, Berkshire, United Kingdom RG4 7BY
 *
 * This Source Code Form is the subject of the following patents and patent
 * applications, owned by 51Degrees Mobile Experts Limited of 5 Charlotte
 * Close, Caversham, Reading, Berkshire, United Kingdom RG4 7BY:
 * European Patent No. 2871816;
 * European Patent Application No. 17184134.9;
 * United States Patent Nos. 9,332,086 and 9,350,823; and
 * United States Patent Application No. 15/686,066.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0.
 *
 * If a copy of the MPL was not distributed with this file, You can obtain
 * one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */

package fiftyone.mobile.detection;

import com.google.common.io.Files;
import fiftyone.mobile.Filename;
import fiftyone.mobile.StandardUnitTest;
import fiftyone.mobile.detection.cache.CacheOptions;
import fiftyone.mobile.detection.cache.LruCache;
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
        IndirectDataset dataset = DatasetBuilder.file()
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
        IndirectDataset dataset = DatasetBuilder.file()
                .configureDefaultCaches()
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

        Dataset dataset = DatasetBuilder.file()
                .lastModified(new Date())
                .build(temp.getPath());
        ViableProvider.ensureViableProvider(new Provider(dataset));
        dataset.close();
        // temp must still exist after close
        assertTrue(temp.exists());

        // assess whether temporary file gets deleted
        dataset = DatasetBuilder.file()
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
        IndirectDataset indirectDataset = DatasetBuilder.file()
                .lastModified(new Date())
                .build(Filename.LITE_PATTERN_V32);
        Iterator it = indirectDataset.profiles.iterator();
        while (it.hasNext()) {
            it.next();
        }
        it.next();
    }

    // tests to see if Stream and Memory load the same thing (no caches)
    @Test
    public void testMemoryStreamDatasetConsistentNoCache () throws IOException {

        IndirectDataset indirectDataset = DatasetBuilder.file()
                .lastModified(new Date())
                .build(Filename.LITE_PATTERN_V32);
        Dataset memoryDataset = MemoryFactory.create(Filename.LITE_PATTERN_V32);

        compareDatasets(indirectDataset, memoryDataset);
    }

    // tests to see if Stream and Memory load the same thing (user supplied partial LRUCache)
    @Test
    public void testMemoryStreamDatasetConsistentPartialLruCache () throws IOException {

        IndirectDataset indirectDataset = DatasetBuilder.file()
                .configureCache(NodesCache, new CacheOptions(20, LruCache.builder()))
                .configureCache(ValuesCache, new CacheOptions(100, LruCache.builder()))
                .configureCache(StringsCache, new CacheOptions(100, LruCache.builder()))
                .build(Filename.LITE_PATTERN_V32);
        Dataset memoryDataset = MemoryFactory.create(Filename.LITE_PATTERN_V32);

        compareDatasets(indirectDataset, memoryDataset);
    }

    // see if stream and memory load same thing with a full set of Guava caches
    @Test
    public void testMemoryStreamDatasetConsistentGuava () throws Exception {

        IndirectDataset indirectDataset = getDatasetWithGuavaCaches();
        Dataset memoryDataset = MemoryFactory.create(Filename.LITE_PATTERN_V32);

        compareDatasets(indirectDataset, memoryDataset);
    }

    // see if cache metrics etc work with Guava cache
    @Test
    public void testGuavaCache () throws Exception {
        Dataset dataset = getDatasetWithGuavaCaches();
        Provider provider = new Provider(dataset, GuavaCache.getUserAgentCache());

        cacheTests(provider);
    }
}
