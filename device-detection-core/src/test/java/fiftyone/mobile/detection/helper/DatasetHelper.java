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

package fiftyone.mobile.detection.helper;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.DatasetBuilder.CacheType;
import fiftyone.mobile.detection.IReadonlyList;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.cache.ICache;
import fiftyone.mobile.detection.IndirectDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;

import static fiftyone.mobile.detection.DatasetBuilder.CacheType.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * helpers to test datasets
 */
public class DatasetHelper {
    private static Logger logger = LoggerFactory.getLogger(DatasetHelper.class);

    public static void compareDatasets(IndirectDataset indirectDataset, Dataset memoryDataset) {
        logger.debug("Strings");
        compareLists(indirectDataset.strings, memoryDataset.strings);
        printDatasetCacheInfo(indirectDataset);

        logger.debug("Signatures");
        compareLists(indirectDataset.signatures, memoryDataset.signatures);
        printDatasetCacheInfo(indirectDataset);

        logger.debug("Profiles");
        compareLists(indirectDataset.profiles, memoryDataset.profiles);
        printDatasetCacheInfo(indirectDataset);

        logger.debug("Nodes");
        compareLists(indirectDataset.nodes, memoryDataset.nodes);
        printDatasetCacheInfo(indirectDataset);

        logger.debug("Values");
        compareLists(indirectDataset.values, memoryDataset.values);
        printDatasetCacheInfo(indirectDataset);
    }



    public static void cacheTests(Provider provider) throws IOException {
        // When ensureViableProvider is called, 2 cache requests are made. 1 for
        // testMap and 1 for testString.
        ViableProvider.ensureViableProvider(provider);
        IndirectDataset dataset = (IndirectDataset) provider.dataSet;
        assertEquals(1, provider.getCacheMisses());
        assertEquals(2, provider.getCacheRequests(), 0);
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
        assertEquals(4, provider.getCacheRequests(), 0);
        assertEquals(stringsRequests, dataset.getCache(StringsCache).getCacheRequests());
        assertEquals(stringsMisses, dataset.getCache(StringsCache).getCacheMisses());
        assertEquals(nodesRequests, dataset.getCache(NodesCache).getCacheRequests());
        assertEquals(nodesMisses, dataset.getCache(NodesCache).getCacheMisses());
        assertEquals(valuesRequests, dataset.getCache(ValuesCache).getCacheRequests());
        assertEquals(valuesMisses, dataset.getCache(ValuesCache).getCacheMisses());
        if (provider.dataSet instanceof IndirectDataset == false)
        {
            // only check this if the dataset is in memory, as the indirect
            // dataset needs to fetch profiles from the cache
            assertEquals(profilesRequests, dataset.getCache(ProfilesCache).getCacheRequests());
        }
        assertEquals(profilesMisses, dataset.getCache(ProfilesCache).getCacheMisses());
        assertEquals(signaturesRequests, dataset.getCache(SignaturesCache).getCacheRequests());
        assertEquals(signaturesMisses, dataset.getCache(SignaturesCache).getCacheMisses());

        // again check that the requests go up and that nothing hits the backend caches
        ViableProvider.ensureViableProvider(provider);
        assertEquals(1, provider.getCacheMisses());
        assertEquals(6, provider.getCacheRequests(), 0);
        assertEquals(stringsRequests, dataset.getCache(StringsCache).getCacheRequests());
        assertEquals(stringsMisses, dataset.getCache(StringsCache).getCacheMisses());
        assertEquals(nodesRequests, dataset.getCache(NodesCache).getCacheRequests());
        assertEquals(nodesMisses, dataset.getCache(NodesCache).getCacheMisses());
        assertEquals(valuesRequests, dataset.getCache(ValuesCache).getCacheRequests());
        assertEquals(valuesMisses, dataset.getCache(ValuesCache).getCacheMisses());
        if (provider.dataSet instanceof IndirectDataset == false)
        {
            // only check this if the dataset is in memory, as the indirect
            // dataset needs to fetch profiles from the cache
            assertEquals(profilesRequests, dataset.getCache(ProfilesCache).getCacheRequests());
        }
        assertEquals(profilesMisses, dataset.getCache(ProfilesCache).getCacheMisses());
        assertEquals(signaturesRequests, dataset.getCache(SignaturesCache).getCacheRequests());
        assertEquals(signaturesMisses, dataset.getCache(SignaturesCache).getCacheMisses());
    }

    public static void printDatasetCacheInfo(IndirectDataset dataset) {
        for (CacheType type: CacheType.values()) {
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

    public static void compareLists(IReadonlyList list1, IReadonlyList list2) {
        assertEquals(list1.size(), list2.size());
        Iterator streamIt = list1.iterator();
        Iterator memoryIt = list1.iterator();
        while (streamIt.hasNext()) {
            assertEquals(streamIt.next().toString(), memoryIt.next().toString());
        }
        assertFalse(memoryIt.hasNext());
    }
}
