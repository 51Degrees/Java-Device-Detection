package fiftyone.mobile.detection;

import java.util.concurrent.ConcurrentHashMap;

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
     * The next time the caches should be switched.
     */
    private long nextCacheService;
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
    
    private final int cacheSize;
    
    public long requests;
    
    public long misses;
    
    private long switches;

    /**
     * Constructs a new instance of the cache.
     * @param cacheSize number of items in this cache lists.
     */
    public Cache(int cacheSize) {
        this.requests = 0;
        this.switches = 0;
        this.misses = 0;
        this.cacheSize = cacheSize;
        cacheServiceSize = (cacheSize / 2);
        active = new ConcurrentHashMap<K, V>(this.cacheSize);
        background = new ConcurrentHashMap<K, V>(this.cacheSize);
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
            switches++;
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

    void setActive(K key, V result) {
        active.putIfAbsent(key, result);
    }

    void setBackground(K key, V result) {
        background.putIfAbsent(key, result);
    }
    
    /**
     * Returns the percentage of times cache request did not return a result.
     * @return Percentage or -1 if an exception occurred.
     */
    public double getPercentageMisses() {
        try {
            return ((double)misses / (double)requests);
        } catch (ArithmeticException aex) {
            return -1;
        }
    }
}
