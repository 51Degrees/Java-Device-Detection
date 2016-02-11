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
package fiftyone.mobile.detection.entities.stream;

import fiftyone.mobile.detection.cache.ICacheLoader;
import fiftyone.mobile.detection.entities.BaseEntity;
import fiftyone.mobile.detection.factories.BaseEntityFactory;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;


/**
 * A readonly list of fixed length entity types held on persistent storage 
 * rather than in memory.
 * <p>
 * Entities in the underlying data structure are either fixed length where the 
 * data that represents them always contains the same number of bytes, or 
 * variable length where the number of bytes to represent the entity varies.
 * <p>
 * This class uses the index of the entity in the accessor. The list is 
 * typically used by entities that need to be found quickly using a divide and 
 * conquer algorithm.
 * <p>
 * The constructor will read the header information about the underlying data 
 * structure. The data for each entity is only loaded when requested via the 
 * accessor. A cache is used to avoid creating duplicate objects when requested 
 * multiple times.
 * <p>
 * Data sources which don't support seeking can not be used. Specifically 
 * compressed data structures can not be used with these lists.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic
 * 
 * @param <T> The type of BaseEntity the list will contain.
 */
public class FixedCacheList<T extends BaseEntity> extends StreamFixedList<T> 
                            implements ICacheLoader<Integer, T>, ICacheList {

    /**
     * Used to store previously accessed items to improve performance and
     * reduce memory consumption associated with creating new instances of 
     * entities already in use.
     */
    protected final Cache<T> cache;
    
    /**
     * Constructs a new instance of FixedCacheList{T}.
     * 
     * @param dataSet The DataSet being created
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     * @param entityFactory Used to create new instances of the entity.
     * @param cacheSize Number of items in list to have capacity to cache.
     */
    public FixedCacheList(Dataset dataSet, BinaryReader reader, 
            BaseEntityFactory<T> entityFactory, int cacheSize) {
        super(dataSet, reader, entityFactory);
        this.cache = new Cache<T>(cacheSize, this);
    }
    
    /**
     * Returns Percentage of request that were not already held in the cache.
     * 
     * @return Percentage of request that were not already held in the cache.
     */
    @Override
    public double getPercentageMisses() {
        return cache != null ? cache.getPercentageMisses() : 0;
    }

    /**
     * Returns the number of request that were not already held in the cache.
     * @return the number of request that were not already held in the cache.
     */
    public double getCacheMisses() {
        return cache != null ? cache.getCacheMisses() : 0;
    }


    /**
     * Returns The number of times the cache has been switched.
     * 
     * @return The number of times the cache has been switched.
     */
    @Override
    @Deprecated
    public long getSwitches() {
        return 0;
    }

    /**
     * Resets the cache list status for the list.
     */
    @Override
    public void resetCache() {
        cache.resetCache();
    }
    
    /**
     * Retrieves the entity at the offset or index requested.
     * 
     * @param key Index or offset of the entity required.
     * @return A new instance of the entity at the offset or index.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @Override
    public T get(int key) throws IOException {
        return cache.get(key);
    }

    /**
     * Increases the cache size. This is used when added values to
     * the cache.
     *
     * @param increaseBy The amount to increase the cache size by.
     * @throws IOException
     */
    public void increaseCacheSize(int increaseBy) throws IOException {
        cache.setCacheSize(cache.getCacheSize() + increaseBy);
    }
    
    /**
     * Used to retrieve items from the underlying list. Called by Cache{T} when
     * a cache miss occurs.
     * 
     * @param key index or offset of the entity required
     * @return he base lists item for the key provided
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @Override
    public T fetch(Integer key) throws IOException {
        return super.get(key);
    }
}