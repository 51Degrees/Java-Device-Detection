package fiftyone.device.proto.example;

import fiftyone.device.proto.Cache;
import fiftyone.device.proto.PluggableCacheProvider;
import fiftyone.device.proto.StreamDatasetBuilder;
import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.Provider;


import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import java.io.IOException;
import java.util.Date;

import static fiftyone.device.proto.StreamDatasetBuilder.CacheType.NodeCache;
import static fiftyone.device.proto.StreamDatasetBuilder.CacheType.PropertyCache;

/**
 * @author jo
 */
class Jsr107Example {

    public static void main (String[] args) throws IOException {
        // not clear yet how to set useful things like max entry etc.
        Configuration configuration = new MutableConfiguration();

        CacheManager manager = Caching.getCachingProvider().getCacheManager();
        javax.cache.Cache nodeCache = manager.createCache("NodeCache", configuration);

        javax.cache.Cache propertyCache = manager.createCache("PropertyCache", configuration);

        javax.cache.Cache uaCache = manager.createCache("UACache", configuration);

        Dataset dataset = new StreamDatasetBuilder()
                .addCache(NodeCache, nodeCache)
                .addCache(PropertyCache, propertyCache)
                .isTempfile()
                .lastModified(new Date())
                .build("51Degrees.dat");

        Provider provider = new PluggableCacheProvider(dataset, uaCache);
    }
}
