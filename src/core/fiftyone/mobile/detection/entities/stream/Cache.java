package fiftyone.mobile.detection.entities.stream;

import fiftyone.mobile.detection.Disposable;
import fiftyone.mobile.detection.factories.BaseEntityFactory;
import fiftyone.properties.DetectionConstants;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
 * the need to re-fetch them from the underlying storage mechanism. <p> The
 * cache works by maintaining two dictionaries of entities keyed on their offset
 * or index. The inactive list contains all items requested since the cache was
 * created or last serviced. The active list contains all the items currently in
 * the cache. The inactive list is always updated when an item is requested. <p>
 * When the cache is serviced the active list is destroyed and the inactive list
 * becomes the active list. i.e. all the items that were requested since the
 * cache was last serviced are now in the cache. A new inactive list is created
 * to store all items being requested since the cache was last serviced.
 *
 * @param T The type of BaseEntity the cache will contain
 */
public class Cache<T> implements Disposable {

    // The active list of cached items.
    ConcurrentHashMap<Integer, T> itemsActive = new ConcurrentHashMap<Integer, T>();
    // The list of inactive cached items.
    ConcurrentHashMap<Integer, T> itemsInactive = new ConcurrentHashMap<Integer, T>();
    // Timer thread used to service the cache.
    private final Timer cacheServiceTimer;
    // The number of requests made to the cache.
    AtomicInteger requests = new AtomicInteger();
    // The number of times an item was not available.
    AtomicInteger misses = new AtomicInteger();
    final BaseEntityFactory<T> entityFactory;

    /**
     * Constructs a new instance of the cache.
     *
     * @param serviceCacheInternal Time between cache services
     */
    Cache(BaseEntityFactory<T> creator) {
        this.entityFactory = creator;
        this.cacheServiceTimer = new Timer();
        cacheServiceTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                ConcurrentHashMap<Integer, T> temp = itemsInactive;
                itemsInactive = new ConcurrentHashMap<Integer, T>();
                itemsActive = temp;
            }
        }, DetectionConstants.CACHE_SERVICE_INTERVAL * 1000,
                DetectionConstants.CACHE_SERVICE_INTERVAL * 1000);
    }

    void addRecent(int index, T item) {
        itemsInactive.put(index, item);
    }

    double getPercentageMisses() {
        return misses.doubleValue() / requests.doubleValue();
    }

    interface EntityCreator<T> {

        T create(int offsetOrIndex);
    }

    @Override
    public void dispose() {
        cacheServiceTimer.cancel();
    }
}
