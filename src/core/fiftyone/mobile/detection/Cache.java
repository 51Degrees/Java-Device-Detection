package fiftyone.mobile.detection;

import java.text.DecimalFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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
 */
public class Cache<K, V> {
    /**
     * When this number of items are in the cache the lists should be switched.
     */
    private final int cacheServiceSize;
    /**
     * The active list of cached items.
     */
    public ConcurrentHashMap<K, V> active;
    /**
     * The list of inactive cached items.
     */
    public ConcurrentHashMap<K, V> background;
    /**
     * The number of items the cache lists should have capacity for.
     */
    private final int cacheSize;
    /**
     * The number of requests made to the cache.
     */
    private final AtomicLong requests;
    /**
     * The number of times an item was not available.
     */
    private final AtomicLong misses;
    /**
     * The number of times the cache was switched.
     */
    private final AtomicInteger switches;
    /**
     * Indicates a switch operation is in progress.
     */
    private boolean switching;

    /**
     * Constructs a new instance of the cache.
     * @param cacheSize The number of items to store in the cache.
     */
    public Cache(int cacheSize) {
        this.switching = false;
        this.requests = new AtomicLong(0);
        this.switches = new AtomicInteger(0);
        this.misses = new AtomicLong(0);
        this.cacheSize = cacheSize;
        cacheServiceSize = (cacheSize / 2);
        active = new ConcurrentHashMap<K, V>(this.cacheSize);
        background = new ConcurrentHashMap<K, V>(this.cacheSize);
    }

    /**
     * Returns the number of times active and inactive lists had to be swapped.
     * @return the number of times active and inactive lists had to be swapped.
     */
    public int getCacheSwitches() {
        return this.switches.intValue();
    }
    
    /**
     * Service the cache by switching the lists if the next service time has
     * passed.
     */
    private void service() {
        // Switch the cache dictionaries over.
        ConcurrentHashMap<K, V> tempCache = active;
        active = background;
        background = tempCache;

        // Clear the background cache before continuing.
        background.clear();
        switches.incrementAndGet();
        
        //Make sure future service will be able to access this block of code.
        switching = false;
    }
    
    /**
     * Indicates an item has been retrieved from the data set and should be 
     * reset in the cache so it's not removed at the next service. If the 
     * inactive cache is now more than 1/2 the total cache size the the lists 
     * should be switched.
     * @param key item key.
     * @param value item value.
     */
    public void addRecent(K key, V value) {
        setBackground(key, value);
        if (background.size() > cacheServiceSize && !switching) {
            synchronized(this) {
                if (background.size() > cacheServiceSize && !switching) {
                    switching = true;
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            service();
                        }
                    }
                    );
                    t.start();
                }
            }
        }
    }

    /**
     * Attempts to get the value with the given, or null if no key is found.
     */
    V tryGetValue(K key) {
        return active.get(key);
    }

    /**
     * Add a new entry to the active list if entry is not already in the list.
     * @param key Some identified that can later be used for entry lookup.
     * @param result The value corresponding to the key.
     */
    void setActive(K key, V result) {
        active.putIfAbsent(key, result);
    }

    /**
     * Add a new entry to the background list if entry is not already in the 
     * list.
     * @param key Some identified that can later be used for entry lookup.
     * @param result The value corresponding to the key.
     */
    void setBackground(K key, V result) {
        background.putIfAbsent(key, result);
    }
    
    /**
     * Returns the percentage of times cache request did not return a result.
     * @return Percentage or -1 if an exception occurred.
     */
    public double getPercentageMisses() {
        try {
            return (getCacheMisses() / getCacheRequests()) * 100;
        } catch (ArithmeticException aex) {
            return -1;
        }
    }
    
    /**
     * Increment the total number of requests to cache by one.
     */
    public void incrementRequestsByOne() {
        requests.incrementAndGet();
    }
    
    /**
     * Returns the current number of total requests to cache.
     * @return the current number of total requests to cache.
     */
    public double getCacheRequests() {
        return requests.doubleValue();
    }
    
    /**
     * Returns the total number of misses for the current cache.
     * @return the total number of misses for the current cache.
     */
    public long getCacheMisses() {
        return misses.get();
    }
    
    /**
     * Increments the number of misses for the current cache by one.
     */
    public void incrementMissesByOne() {
        misses.incrementAndGet();
    }
    
    /**
     * Clear the active list.
     */
    public void clearActiveList() {
        this.active.clear();
    }
    
    /**
     * Clear the background list.
     */
    public void clearBackgroundList() {
        this.background.clear();
    }
    
    /**
     * Reset the value of misses to 0.
     */
    public void clearMisses() {
        this.misses.set(0);
    }
    
    /**
     * Reset the value of switches to 0.
     */
    public void clearSwitches() {
        this.switches.set(0);
    }
    
    /**
     * Reset the value of requests to 0.
     */
    public void clearRequests() {
        this.requests.set(0);
    }
}
