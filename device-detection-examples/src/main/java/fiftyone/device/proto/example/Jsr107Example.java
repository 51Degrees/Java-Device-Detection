package fiftyone.device.proto.example;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import java.io.IOException;

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

/*
        TODO put something in here
        Dataset dataset = new StreamDatasetBuilder()
                .addCache(NodeCache, nodeCache)
                .addCache(PropertyCache, propertyCache)
                .isTempfile()
                .lastModified(new Date())
                .build("51Degrees.dat");

        Provider provider = new PluggableCacheProvider(dataset, uaCache);
*/
    }
}
