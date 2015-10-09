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
package fiftyone.mobile.detection.entities.stream;

import fiftyone.mobile.detection.entities.BaseEntity;
import fiftyone.mobile.detection.factories.BaseEntityFactory;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;

/**
 * Lists can be stored as a set of related objects entirely within memory, or 
 * the relevant objects loaded as required from a file or other permanent store
 * as required.
 * 
 * This class provides core functions needed for lists which load objects
 * as required. It implements the Cache{T} to store frequently requested
 * objects and improve memory usage and performance.
 * 
 * Delegate methods are used to create new instances of items to add to the 
 * list in order to avoid creating many inherited list classes for each 
 * BaseEntity type.
 * 
 * Should not be referenced directly.
 * 
 * @param <T> The type of BaseEntity the list will contain
 */
public abstract class StreamCacheList<T extends BaseEntity> 
                            extends StreamBaseList<T> implements ICacheList {
    /**
     * Used to store previously accessed items to improve performance and
     * reduce memory consumption associated with creating new instances of 
     * entities already in use.
     */
    private final Cache<T> cache;
    
    /**
     * Constructs a new instance of StreamBaseList{T} ready to read entities 
     * from the source.
     * @param dataSet Dataset being created.
     * @param reader Reader used to initialise the header only.
     * @param entityFactory Used to create new instances of the entity.
     * @param cacheSize Number of items in list to have capacity to cache.
     */
    public StreamCacheList(Dataset dataSet, BinaryReader reader, 
                            BaseEntityFactory<T> entityFactory, int cacheSize) {
        super(dataSet, reader, entityFactory);
        this.cache = new Cache<T>(cacheSize);
    }

    /**
     * Retrieves the entity at the offset or index requested.
     * @param key Index or offset of the entity required.
     * @return A new instance of the entity at the offset or index.
     */
    @Override
    public T get(int key) {
        T item = cache.active.get(key);
        if (item == null) {
            try {
                item = super.get(key);
                cache.active.put(key, item);
                cache.incrementMissesByOne();
            } catch (IOException ex) {
                System.err.println("StreamCacheList: failed to retrieve an "
                        + "element at position: "+key);
            }
        }
        cache.addRecent(key, item);
        cache.incrementRequestsByOne();
        return item;
    }

    /**
     * Returns Percentage of request that were not already held in the cache.
     * @return Percentage of request that were not already held in the cache.
     */
    @Override
    public double getPercentageMisses() {
        return (cache != null ? cache.getPercentageMisses() : 0);
    }

    /**
     * Returns The number of times the cache has been switched.
     * @return The number of times the cache has been switched.
     */
    @Override
    public long getSwitches() {
        return (cache != null ? cache.getCacheSwitches() : 0);
    }

    /**
     * Resets the cache.
     */
    @Override
    public void resetCache() {
        cache.resetCache();
    }
}
