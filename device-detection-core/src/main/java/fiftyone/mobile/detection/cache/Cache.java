/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2015 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Many of the entities used by the detector data set are requested repeatedly. 
 * The cache improves memory usage and reduces strain on the garbage collector
 * by storing previously requested entities for a short period of time to avoid 
 * the need to re-fetch them from the underlying storage mechanism.
 * 
 * The cache works by maintaining two dictionaries of entities keyed on their 
 * offset or index. The inactive list contains all items requested since the 
 * cache was created or last serviced. The active list contains all the items 
 * currently in the cache. The inactive list is always updated when an item is 
 * requested.
 * 
 * When the cache is serviced the active list is destroyed and the inactive list
 * becomes the active list. i.e. all the items that were requested since the 
 * cache was last serviced are now in the cache. A new inactive list is created 
 * to store all items being requested since the cache was last serviced.
 *
 * @param <K> Key for the cache items.
 * @param <V> Value for the cache items.
 * @param <S> Source used to fetch items not in the cache.
 */
public class Cache<K, V, S extends ICacheLoader<K, V>> {
    
    /**
     * Loader used to fetch items not in the cache.
     */
    private final S loader;
    /**
     * The second hashmap of cached items.
     */
    volatile private ConcurrentHashMap<K, V> back;
    /**
     * The first hashmap of cached items.
     */
    volatile private ConcurrentHashMap<K, V> front;
    /**
     * When this number of items are in the front hashmap the cache should be 
     * switched.
     */
    private final int cacheServiceSize;
    /**
     * The number of requests made to the cache.
     */
    private final AtomicLong requests = new AtomicLong(0);
    /**
     * The number of times an item was not available.
     */
    private final AtomicLong misses = new AtomicLong(0);
    /**
     * The number of times the cache was switched.
     */
    private final AtomicLong switches = new AtomicLong(0);
    /**
     * Indicates a switch operation is in progress.
     */
    volatile private boolean switching = false;

    /**
     * Constructs a new instance of the cache.
     * @param cacheSize The number of items to store in the cache.
     */
    public Cache(int cacheSize) {
        this(cacheSize, null);
    }
    
    /**
     * Constructs a new instance of the cache.
     * @param cacheSize The number of items to store in the cache.
     * @param loader used to fetch items not in the cache.
     */    
    public Cache(int cacheSize, S loader) {
        cacheServiceSize = (cacheSize / 2);
        front = new ConcurrentHashMap<K, V>(cacheSize);
        back = new ConcurrentHashMap<K, V>(cacheSize);
        this.loader = loader;
    }

    /**
     * @return number of misses
     */
    public long getCacheMisses() {
        return misses.get();
    }
    
    /**
     * @return number of requests
     */
    public long getCacheRequests() {
        return requests.get();
    }
    
    /**
     * @return the percentage of times cache request did not return a result.
     */
    public double getPercentageMisses() {
        return misses.doubleValue()/ requests.doubleValue();
    }
    
    /**
     * Returns the number of times active and inactive lists had to be swapped.
     * @return the number of times active and inactive lists had to be swapped.
     */
    public int getCacheSwitches() {
        return this.switches.intValue();
    }
    
    /**
     * Retrieves the value for key requested. If the key does not exist
     * in the cache then the Fetch method of the cache's loader is used to
     * retrieve the value.
     * @param key or the item required
     * @return An instance of the value associated with the key
     * @throws java.io.IOException
     */    
    public V get(K key) throws IOException {
        return get(key, loader);
    }
    
    /**
     * Retrieves the value for key requested. If the key does not exist
     * in the cache then the Fetch method is used to retrieve the value
     * from another loader.
     * @param key or the item required
     * @param loader to fetch the items from
     * @return An instance of the value associated with the key
     * @throws java.io.IOException
     */
    public V get(K key, S loader) throws IOException {
        V value = front.get(key);
        if (value == null) {
            value = back.get(key);
            if (value == null) {
                value = loader.fetch(key);
                back.putIfAbsent(key, value);
                this.misses.incrementAndGet();
            }
            front.putIfAbsent(key, value);
            checkForService();
        }
        this.requests.incrementAndGet();
        return value;
    }
    
    /**
     * Resets the stats for the cache.
     */
    public void resetCache()
    {
        this.back.clear();
        this.front.clear();
        misses.set(0);
        requests.set(0);
        switches.set(0);
    }

    /// <summary>
    /// Check to see if the Front and Back dictionaries should be switched
    /// over.
    /// </summary>
    private void checkForService()
    {
        if (front.size() > cacheServiceSize &&
            switching == false)
        {
            synchronized(this)
            {
                if (front.size() > cacheServiceSize &&
                    switching == false)
                {
                    service();
                }
            }
        }
    }

    /**
     * Service the cache by switching the lists if the next service time has
     * passed.
     */
    private void service() {
        // Switch the cache dictionaries over.
        ConcurrentHashMap<K, V> temp = front;
        front = back;
        back = temp;

        // Clear the back cache before continuing.
        front.clear();
        switches.incrementAndGet();
        
        // Make sure future service will be able to access this block of code.
        switching = false;
    }
}
