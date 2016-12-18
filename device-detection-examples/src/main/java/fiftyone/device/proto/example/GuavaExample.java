package fiftyone.device.proto.example;

import com.google.common.cache.CacheBuilder;
import fiftyone.device.proto.Cache;
import fiftyone.device.proto.PluggableCacheProvider;
import fiftyone.device.proto.StreamDatasetBuilder;
import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.Match;
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
class GuavaExample {

    public static void main (String[] args) throws IOException {
        com.google.common.cache.Cache uaCache = CacheBuilder.newBuilder()
                .initialCapacity(1000)
                .maximumSize(100000)
                .concurrencyLevel(5)
                .build();

        com.google.common.cache.Cache nodeCache = CacheBuilder.newBuilder()
                .initialCapacity(10000)
                .maximumSize(10000)
                .build();

        com.google.common.cache.Cache propertyCache = CacheBuilder.newBuilder()
                .initialCapacity(10000)
                .maximumSize(10000)
                .build();

        Dataset dataset = new StreamDatasetBuilder()
                .addCache(NodeCache, nodeCache)
                .addCache(PropertyCache, propertyCache)
                .isTempfile()
                .lastModified(new Date())
                .build("51Degrees.dat");

        Provider provider = new PluggableCacheProvider(dataset, new Cache.GuavaAdaptor(uaCache));

        Match match = provider.match("Hello World");
    }
}
