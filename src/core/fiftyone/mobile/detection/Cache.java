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
 * @param K
 * @param V
 */
class Cache<K, V> {

    /**
     * The next time the caches should be switched.
     */
    private long nextCacheService;
    /**
     * The time between cache services.
     */
    private int serviceIntervalMS;
    /**
     * The active cache.
     */
    private ConcurrentHashMap<K, V> active;
    /**
     * The background cache.
     */
    private ConcurrentHashMap<K, V> background;

    /**
     * Constructs a new instance of the cache.
     * @param serviceInterval number of seconds between switching the cache.
     */
    public Cache(int serviceInterval) {
        serviceIntervalMS = serviceInterval + 1000;
        nextCacheService = System.currentTimeMillis() + serviceIntervalMS;
        active = new ConcurrentHashMap<K, V>();
        background = new ConcurrentHashMap<K, V>();
    }

    /**
     * Service the cache by switching the lists if the next service time has
     * passed.
     */
    private void service() {
        if (nextCacheService < System.currentTimeMillis()) {
            synchronized(this) {
                if (nextCacheService < System.currentTimeMillis()) {
                    // Switch the cache dictionaries over.
                    ConcurrentHashMap<K, V> tempCache = active;
                    active = background;
                    background = tempCache;

                    // Clear the background cache before continuing.
                    background.clear();
                    
                    // Set the next service interval.
                    nextCacheService = System.currentTimeMillis() + 
                            serviceIntervalMS;
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

    void setActive(K key, V result) {
        active.putIfAbsent(key, result);
    }

    void setBackground(K key, V result) {
        background.putIfAbsent(key, result);
        service();
    }
}
