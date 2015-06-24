package fiftyone.mobile.detection;

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
 * Used to speed the retrieval of detection results over duplicate requests.
 *
 * @param <K>
 * @param <V>
 */
public class Cache<K, V> {
    /**
     * The time between cache services.
     */
    private final int cacheServiceSize;
    /**
     * The active cache.
     */
    public ConcurrentHashMap<K, V> active;
    /**
     * The background cache.
     */
    private ConcurrentHashMap<K, V> background;
    /**
     * Number of items in cache.
     */
    private final int cacheSize;
    /**
     * Total number of requests to cache.
     */
    private final AtomicLong requests;
    /**
     * The number of requests that could not be found in active cache.
     */
    private final AtomicLong misses;
    /**
     * Number of times background list had to be switched with the active list.
     */
    private final AtomicInteger switches;

    /**
     * Constructs a new instance of the cache.
     * @param cacheSize number of items in this cache lists.
     */
    public Cache(int cacheSize) {
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
        synchronized(this) {
            // Switch the cache dictionaries over.
            ConcurrentHashMap<K, V> tempCache = active;
            active = background;
            background = tempCache;

            // Clear the background cache before continuing.
            background.clear();
            switches.incrementAndGet();
        }
    }
    
    /**
     * Add new entry to the background list. Once background list becomes 
     * larger than the active list, the lists are switched. The background list 
     * becomes active and vice versa. Service task is also run.
     * @param key item key.
     * @param value item value.
     */
    public void addRecent(K key, V value) {
        setBackground(key, value);
        if (background.size() > cacheServiceSize) {
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
            return ((double)getCacheMisses() / (double)getCacheRequests() * 100);
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
}
