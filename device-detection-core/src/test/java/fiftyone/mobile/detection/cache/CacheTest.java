/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
 * ********************************************************************* */
package fiftyone.mobile.detection.cache;

import fiftyone.mobile.detection.DetectionTestSupport;
import fiftyone.mobile.detection.cache.Cache.KeyValuePair;
import fiftyone.mobile.detection.test.TestType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(TestType.TypeUnit.class)
public class CacheTest extends DetectionTestSupport {

    public class CacheLoader<K, V> implements ICacheLoader<K, V> {

        /**
         * Number of times items have been fetched from the source.
         */
        private final AtomicInteger fetches = new AtomicInteger(0);

        /**
         * Source of data to be used by the loader.
         */
        private final HashMap<K, V> source;

        CacheLoader(HashMap<K, V> source) {
            this.source = source;
        }

        @Override
        public V fetch(K key) throws IOException {
            fetches.incrementAndGet();
            return source.get(key);
        }
    }

    @Test
    @Category(TestType.TypeUnit.class)
    public void single() throws IOException {
        HashMap<Integer, String> source = getNumericKeys(1);
        CacheLoader<Integer, String> loader =
                new CacheLoader<Integer, String>(source);
        Cache<Integer, String> cache =
                new Cache<Integer, String>(source.size(), loader);
        assertTrue(cache.get(0).equals(source.get(0)));
        assertTrue(cache.getCacheMisses() == 1);
    }

    @Test
    public void singleNull() {
        HashMap<Integer, String> source = getNumericKeys(1);
        CacheLoader<Integer, String> loader =
                new CacheLoader<Integer, String>(source);
        Cache<Integer, String> cache = new Cache<Integer, String>(
                source.size(),
                loader);
        try {
            assertTrue(cache.get(1) == null);
        } catch (Exception ignored) {
        }
        assertTrue(cache.getCacheMisses() == 1);
    }

    @Test
    public void numericFull() throws IOException {
        validateCache(getNumericKeys(10000));
    }

    @Test
    public void stringFull() throws IOException {
        validateCache(getRandomStringKeys(10000));
    }

    @Test
    public void performance() throws IOException {
        HashMap<String, String> source = getRandomStringKeys(1000000);
        CacheLoader<String, String> loader =
                new CacheLoader<String, String>(source);
        Cache<String, String> cache =
                new Cache<String, String>(source.size(), loader);
        long fill = processSource(source, cache);
        long retrieve = processSource(source, cache);
        logger.info(
                "Cache fill time of '%d' milliseconds.",
                fill);
        logger.info(
                "Cache retrieve time of '%d' milliseconds.",
                retrieve);
        logger.info(
                "Cache performance increase of '{%f.2}' times.",
                fill / retrieve);
        if (retrieve >= fill) {
            fail("Cache retrieve time should be lower than fill time.");
        }
    }

    /**
     * Processes the source against the cache.
     */
    private long processSource(
            HashMap<String, String> source,
            Cache<String, String> cache) throws IOException {
        long start = System.currentTimeMillis();
        for (String key : source.keySet()) {
            assertTrue(cache.get(key).equals(source.get(key)));
        }
        return System.currentTimeMillis() - start;
    }

    /**
     * Takes a source of cacheable items and tests the cache statistics
     * using fixed loading and retrieval patterns.
     */
    private <K, V> void validateCache(HashMap<K, V> source) throws IOException {
        CacheLoader<K, V> loader = new CacheLoader<K, V>(source);
        Cache<K, V> cache = new Cache<K, V>(source.size() / 2, loader);
        List<K> keysFront = new ArrayList<K>();
        List<K> keysBack = new ArrayList<K>();

        // Prepare the front and back lists.
        List<K> currentList = keysFront;
        int count = 0;
        for (K key : source.keySet()) {
            currentList.add(key);
            count++;
            if (count == source.size() / 2) {
                currentList = keysBack;
            }
        }

        // Fill the cache with half of the values.
        for (K key : keysFront) {
            Cache.Node expectedLast = cache.linkedList.last;
            assertTrue(cache.get(key) == source.get(key));
            KeyValuePair kvp = (KeyValuePair)cache.linkedList.first.item;
            assertTrue(kvp.key == key);
            assertTrue(expectedLast == null ||
                    expectedLast == cache.linkedList.last);
        }
        assertTrue(cache.getCacheMisses() == loader.fetches.get());
        assertTrue(cache.getCacheMisses() == source.size() / 2);
        assertTrue(cache.getCacheRequests() == source.size() / 2);

        // Check all the values are returned from the cache.
        for (K key : keysFront) {
            Cache.Node expectedLast = cache.linkedList.last.previous;
            assertTrue(cache.get(key) == source.get(key));
            KeyValuePair kvp = (KeyValuePair)cache.linkedList.first.item;
            assertTrue(kvp.key == key);
            assertTrue(expectedLast == null ||
                    expectedLast == cache.linkedList.last);
        }
        assertTrue(cache.getCacheMisses() == loader.fetches.get());
        assertTrue(cache.getCacheMisses() == source.size() / 2);
        assertTrue(cache.getCacheRequests() == source.size());

        // Now use the 2nd half of the source to push out all 
        // the first half.
        for (K key : keysBack) {
            Cache.Node expectedLast = cache.linkedList.last.previous;
            assertTrue(cache.get(key) == source.get(key));
            KeyValuePair kvp = (KeyValuePair)cache.linkedList.first.item;
            assertTrue(kvp.key == key);
            assertTrue(expectedLast == null ||
                    expectedLast == cache.linkedList.last);
        }
        assertTrue(cache.getCacheMisses() == loader.fetches.get());
        assertTrue(cache.getCacheMisses() == source.size());
        assertTrue(cache.getCacheRequests() == source.size() * 1.5);

        // Still using the 2nd half of the source retrieve all
        // the values again. They should come from the cache.
        for (K key : keysBack) {
            Cache.Node expectedLast = cache.linkedList.last.previous;
            assertTrue(cache.get(key) == source.get(key));
            KeyValuePair kvp = (KeyValuePair)cache.linkedList.first.item;
            assertTrue(kvp.key == key);
            assertTrue(expectedLast == null ||
                    expectedLast == cache.linkedList.last);
        }
        assertTrue(cache.getCacheMisses() == loader.fetches.get());
        assertTrue(cache.getCacheMisses() == source.size());
        assertTrue(cache.getCacheRequests() == source.size() * 2);

        // Check that the 1st half of the source is now fetched
        // again and are not already in the cache.
        for (K key : keysFront) {
            Cache.Node expectedLast = cache.linkedList.last.previous;
            assertTrue(cache.get(key) == source.get(key));
            KeyValuePair kvp = (KeyValuePair)cache.linkedList.first.item;
            assertTrue(kvp.key == key);
            assertTrue(expectedLast == null ||
                    expectedLast == cache.linkedList.last);
        }
        assertTrue(cache.getCacheMisses() == loader.fetches.get());
        assertTrue(cache.getCacheMisses() == source.size() * 1.5);
        assertTrue(cache.getCacheRequests() == source.size() * 2.5);

        // Go through in random order and check there are no cache 
        // missed.
        List<K> random = new ArrayList<K>();
        random.addAll(keysFront);
        Collections.shuffle(random, new Random(System.nanoTime()));
        for (K key : random) {
            Cache.Node expectedLast = cache.linkedList.last;
            KeyValuePair kvpExpectedLast = (KeyValuePair)expectedLast.item;
            assertTrue(cache.get(key) == source.get(key));
            KeyValuePair kvp = (KeyValuePair)cache.linkedList.first.item;
            assertTrue(kvp.key == key);
            assertTrue(kvpExpectedLast.key == key ||
                    expectedLast == cache.linkedList.last);
        }
        assertTrue(cache.getCacheMisses() == loader.fetches.get());
        assertTrue(cache.getCacheMisses() == source.size() * 1.5);
        assertTrue(cache.getCacheRequests() == source.size() * 3);
    }

    /**
     * Creates a list of numeric values from 0 to size.
     */
    private HashMap<Integer, String> getNumericKeys(int size) {
        HashMap<Integer, String> source = new HashMap<Integer, String>();
        for (Integer i = 0; i < size; i++) {
            source.put(i, i.toString());
        }
        return source;
    }

    /**
     * Creates a list of random values keyed on a unique string key.
     */
    private static HashMap<String, String> getRandomStringKeys(int size) {
        HashMap<String, String> source = new HashMap<String, String>(size);
        for (int i = 0; i < size; i++) {
            source.put(
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString());
        }
        return source;
    }
}