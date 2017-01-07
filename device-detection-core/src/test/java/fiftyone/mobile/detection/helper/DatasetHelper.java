package fiftyone.mobile.detection.helper;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.DatasetBuilder.CacheType;
import fiftyone.mobile.detection.IReadonlyList;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.cache.ICache;
import fiftyone.mobile.detection.entities.stream.StreamDataset;
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

    public static void compareDatasets(StreamDataset streamDataset, Dataset memoryDataset) {
        logger.debug("Strings");
        compareLists(streamDataset.strings, memoryDataset.strings);
        printDatasetCacheInfo(streamDataset);

        logger.debug("Signatures");
        compareLists(streamDataset.signatures, memoryDataset.signatures);
        printDatasetCacheInfo(streamDataset);

        logger.debug("Profiles");
        compareLists(streamDataset.profiles, memoryDataset.profiles);
        printDatasetCacheInfo(streamDataset);

        logger.debug("Nodes");
        compareLists(streamDataset.nodes, memoryDataset.nodes);
        printDatasetCacheInfo(streamDataset);

        logger.debug("Values");
        compareLists(streamDataset.values, memoryDataset.values);
        printDatasetCacheInfo(streamDataset);
    }



    public static void cacheTests(Provider provider) throws IOException {
        ViableProvider.ensureViableProvider(provider);
        StreamDataset dataset = (StreamDataset) provider.dataSet;
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

    public static void printDatasetCacheInfo(StreamDataset dataset) {
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
