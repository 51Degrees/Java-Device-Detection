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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
public class SwitchingCache<K, V> implements Cache<K,V> {

    /**
     * When this number of items are in the cache the lists should be switched.
     */
    private final int cacheServiceSize;
    /**
     * The active list of cached items.
     */
    private volatile ConcurrentHashMap<K, V> active;
    /**
     * The list of inactive cached items.
     */
    private volatile ConcurrentHashMap<K, V> background;
    /**
     * The number of times the cache was switched.
     */
    private final AtomicInteger switches;
    /**
     * Indicates a switch operation is in progress.
     */
    private volatile boolean switching;

    private CacheStats stats;
    private Loader<K, V> loader;

    /**
     * Constructs a new instance of the cache.
     * @param cacheSize The number of items to store in the cache.
     */
    public SwitchingCache(int cacheSize) {
        this.stats = new CacheStats();
        this.switching = false;
        this.switches = new AtomicInteger(0);

        //The number of items the cache lists should have capacity for.
        cacheServiceSize = (cacheSize / 2);
        active = new ConcurrentHashMap<K, V>(cacheSize);
        background = new ConcurrentHashMap<K, V>(cacheSize);
    }

    @Override
    public boolean enableStats(boolean enable) {
        boolean result = stats.isEnabled();
        stats.setEnabled(enable);
        return result;
    }

    @Override
    public V get(K key) {
        return get(key, loader);
    }

    @Override
    public V get(K key, V resultInstance) {
        V result = getIfCached(key);
        if (result == null) {
            result = loader.load(key, resultInstance);
            if (result != null) {
                active.putIfAbsent(key, result);
                addRecent(key, result);
            }
        }
        return result;
    }

    @Override
    public V getIfCached(K key) {
        stats.incRequests();
        V result = active.get(key);
        if (result != null) {
            addRecent(key, result);
        }
        stats.incMisses();
        return result;
    }

    @Override
    public V get(K key, Loader<K, V> loader) {
        V result = getIfCached(key);
        if (result != null) {
            return result;
        }
        result = loader.load(key);
        if (result == null) {
            return null;
        }
        active.putIfAbsent(key, result);
        addRecent(key, result);
        return result;
    }

    /**
     * Service the cache by switching the lists
     */
    private void service() {
        // Switch the cache dictionaries over.
        ConcurrentHashMap<K, V> tempCache = active;
        active = background;
        background = tempCache;

        // Clear the background cache before continuing.
        background.clear();
        switches.incrementAndGet();
        
        // Make sure future service will be able to access this block of code.
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
    private void addRecent(K key, V value) {
        background.putIfAbsent(key, value);
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

    public double getPercentageMisses() {
        return (double)getCacheMisses() / (double)getCacheRequests();
    }

    @Override
    public long getCacheRequests() {
        return stats.getRequests();
    }

    @Override
    public long getCacheMisses() {
        return stats.getMisses();
    }

    @Override
    public void resetCache() {
        clearActiveList();
        clearBackgroundList();
        clearMisses();
        clearRequests();
        clearSwitches();
    }

    @Override
    public void clearRequests() {
        stats.clearRequests();
    }

    @Override
    public void clearMisses() {
        stats.clearMisses();
    }

    @Override
    public void setLoader(Loader<K, V> loader) {
        this.loader = loader;
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
     * Returns the number of times active and inactive lists had to be swapped.
     * @return the number of times active and inactive lists had to be swapped.
     */
    public int getCacheSwitches() {
        return this.switches.intValue();
    }

    /**
     * Reset the value of switches to 0.
     */
    public void clearSwitches() {
        this.switches.set(0);
    }
}
