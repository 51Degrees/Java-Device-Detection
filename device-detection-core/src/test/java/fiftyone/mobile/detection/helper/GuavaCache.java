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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fiftyone.mobile.Filename;
import fiftyone.mobile.detection.DatasetBuilder;
import fiftyone.mobile.detection.cache.*;
import fiftyone.mobile.detection.IndirectDataset;

import java.io.IOException;
import java.util.Date;

import static fiftyone.mobile.detection.DatasetBuilder.CacheType.*;
import static fiftyone.mobile.detection.DatasetBuilder.*;

/**
 * Example user supplied class providing a Guava Cache
 */
public class GuavaCache {

     static class CacheAdaptor <K,V>  implements ICache<K,V> {
        final Cache<K,V> cache;

        CacheAdaptor(Cache<K, V> cache) {
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

    static class PutCacheAdaptor<K,V> extends CacheAdaptor<K,V> implements IPutCache<K,V>{

        PutCacheAdaptor(Cache<K, V> cache) {
            super(cache);
        }

        @Override
        public void put(K key, V value) {
            cache.put(key, value);
        }
    }

    static class UaCacheAdaptor <K,V> extends PutCacheAdaptor<K,V> implements ILoadingCache<K,V> {

        UaCacheAdaptor(com.google.common.cache.Cache<K,V> cache) {
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

    static class GuavaCacheBuilder implements ICacheBuilder{
         public ICache build(int size){
             com.google.common.cache.Cache guavaCache = CacheBuilder.newBuilder()
                     .initialCapacity(size)
                     .maximumSize(size)
                     .recordStats()
                     .build();

             return new PutCacheAdaptor(guavaCache);
         }
    }

    public static IndirectDataset getDatasetWithGuavaCaches() throws IOException {
        ICacheBuilder builder = new GuavaCacheBuilder();

        @SuppressWarnings("unchecked")
        IndirectDataset dataset =
                DatasetBuilder.file()
                        .setCacheBuilder(NodesCache, builder)
                        .setCacheBuilder(ProfilesCache, builder)
                        .setCacheBuilder(StringsCache, builder)
                        .setCacheBuilder(ValuesCache, builder)
                        .setCacheBuilder(SignaturesCache, builder)
                        .lastModified(new Date())
                        .build(Filename.LITE_PATTERN_V32);

        return dataset;


    }

    public static <K,V> ILoadingCache<K,V> getUserAgentCache() {
        com.google.common.cache.Cache<K,V> uaCache = CacheBuilder.newBuilder()
                .initialCapacity(20)
                .maximumSize(20)
                .recordStats()
                .build();

        return new UaCacheAdaptor<K,V>(uaCache);
    }
}
