/*
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited.
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
 * Caversham, Reading, Berkshire, United Kingdom RG4 7BY
 *
 * This Source Code Form is the subject of the following patent
 * applications, owned by 51Degrees Mobile Experts Limited of 5 Charlotte
 * Close, Caversham, Reading, Berkshire, United Kingdom RG4 7BY:
 * European Patent Application No. 13192291.6; and
 * United States Patent Application Nos. 14/085,223 and 14/085,301.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0.
 *
 * If a copy of the MPL was not distributed with this file, You can obtain
 * one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package fiftyone.device.example.illustration;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fiftyone.device.example.Shared;
import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.DatasetBuilder;
import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.cache.*;

import java.io.IOException;
import java.util.Date;

import static fiftyone.mobile.detection.DatasetBuilder.CacheType.NodesCache;
import static fiftyone.mobile.detection.DatasetBuilder.CacheType.ProfilesCache;
import static fiftyone.mobile.detection.DatasetBuilder.CacheType.SignaturesCache;
import static fiftyone.mobile.detection.DatasetBuilder.NODES_CACHE_SIZE;
import static fiftyone.mobile.detection.DatasetBuilder.PROFILES_CACHE_SIZE;

/**
 * <!-- tutorial -->
 * This Example shows how to:
 * <ol>
 *  <li>use DataSetBuilder to supply a custom cache configuration to
 *  the 51 Degrees device detection API.
 *  <pre class="prettyprint lang-java">
 *  <code>
 *      Dataset dataset = DatasetBuilder.file()
 *          .configureCachesFromCacheSet(DatasetBuilder.CacheTemplate.MultiThreadLowMemory)
 *          .setCacheBuilder(NodesCache, builder)
 *          .setCacheBuilder(ProfilesCache, null)
 *          .lastModified(new Date())
 *          .build(Shared.getLitePatternV32());
 *  </code>
 *  </pre>
 * </ol>
 * <!-- tutorial -->
 * main assumes it is being run with a working directory at root of
 * project or of this module.
 */
public class CachingConfiguration {

    @SuppressWarnings("WeakerAccess")

    /**
     * Class adapting a google Guava cache to the 51 Degrees ICache interface.
     * This allows it to be used by the device detection API.
     */
    public static class GuavaCacheAdaptor<K,V>  implements ICache<K,V> {
        protected final Cache<K,V> cache;

        public GuavaCacheAdaptor(Cache<K,V> cache) {
            this.cache = cache;
        }

        @Override
        public V get(K key) {
            return cache.getIfPresent(key);
        }

        @Override
        public long getCacheSize() {
            return cache.size();
        }

        @Override
        public long getCacheMisses() {
            return cache.stats().missCount();
        }

        @Override
        public long getCacheRequests() {
            return cache.stats().requestCount();
        }

        @Override
        public double getPercentageMisses() {
            return getCacheMisses()/getCacheRequests();
        }

        @Override
        public void resetCache() {
            cache.invalidateAll();
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class PutCacheAdaptor<K,V> extends GuavaCacheAdaptor<K,V> implements IPutCache<K,V>{

        public PutCacheAdaptor(Cache<K, V> cache) {
            super(cache);
        }

        @Override
        public void put(K key, V value) {
            cache.put(key, value);
        }
    }

    @SuppressWarnings("WeakerAccess")
    /**
     * The user agent cache used by the Provider requires a cache that implements ILocadingCache.
     * This class extends the Guava cache adapter to allow it to be used as a loading cache.
     */
    public static class UaCacheAdaptor <K,V> extends GuavaCacheAdaptor<K,V> implements ILoadingCache<K,V> {

        public UaCacheAdaptor(com.google.common.cache.Cache<K,V> cache) {
            super(cache);
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

    /**
     * For a cache to be used by the device detection API, it also needs a corresponding builder
     * that implements the ICacheBuidler interface.
     * This is the ICacheBuilder implementation for the Guava cache.
     */
    static class GuavaCacheBuilder implements ICacheBuilder {
        public ICache build(int size){
            com.google.common.cache.Cache guavaCache = CacheBuilder.newBuilder()
                    .initialCapacity(size)
                    .maximumSize(size)
                    .concurrencyLevel(5) // set to number of threads that can access cache at same time
                    .build();

            return new PutCacheAdaptor(guavaCache);
        }
    }

    // Snippet Start
    public static void main (String[] args) throws IOException {
        // Create the guava cache builder for the non-user agent caches.
        ICacheBuilder builder = new GuavaCacheBuilder();

        @SuppressWarnings("unchecked")
        // Create the dataset using DatasetBuilder
        Dataset dataset = DatasetBuilder.file()
                // First, use the configuration from the MultiThreadLowMemory template.
                .configureCachesFromCacheSet(DatasetBuilder.CacheTemplate.MultiThreadLowMemory)
                // Next, set the signature cache to use the Guava cache builder.
                .setCacheBuilder(SignaturesCache, builder)
                // Set the profile cache to null, this means that profiles will not
                // be stored in memory and will always be loaded from disk when needed.
                .setCacheBuilder(ProfilesCache, null)
                .lastModified(new Date())
                .build(Shared.getLitePatternV32());

        // Create a new guava cache to hold user agents and their corresponding match objects.
        com.google.common.cache.Cache uaCache = CacheBuilder.newBuilder()
                .initialCapacity(100000)
                .maximumSize(100000)
                .concurrencyLevel(5) // set to number of threads that can access cache at same time
                .build();

        @SuppressWarnings("unchecked")
        // Create the provider using the guava cache we've just created.
        Provider provider = new Provider(dataset, new UaCacheAdaptor(uaCache));

        // Use the provider to obtain a match on a user agent
        // User-Agent string of a iPhone mobile device.
        String mobileUserAgent = "Mozilla/5.0 (iPhone; CPU iPhone "
                + "OS 7_1 like Mac OS X) AppleWebKit/537.51.2 (KHTML, like Gecko) "
                + "Version/7.0 Mobile/11D167 Safari/9537.53";

        Match match = provider.match(mobileUserAgent);
        System.out.printf("%s", match.getSignature());
    }
    // Snippet End
}
