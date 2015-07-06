/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fiftyone.mobile.detection.entities.stream;

import fiftyone.mobile.detection.entities.BaseEntity;
import fiftyone.mobile.detection.factories.BaseEntityFactory;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mike
 * @param <T>
 */
public class FixedCacheList<T extends BaseEntity> extends StreamFixedList<T> implements ICacheList {

    /**
     * Used to store previously accessed items to improve performance and
     * reduce memory consumption associated with creating new instances of 
     * entities already in use.
     */
    protected final Cache<T> cache;
    
    /**
     * Constructs a new instance of FixedCacheList{T}.
     * @param dataSet The DataSet being created
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     * @param entityFactory Used to create new instances of the entity.
     * @param cacheSize Number of items in list to have capacity to cache.
     */
    public FixedCacheList(Dataset dataSet, BinaryReader reader, 
            BaseEntityFactory<T> entityFactory, int cacheSize) {
        super(dataSet, reader, entityFactory);
        this.cache = new Cache<T>(cacheSize);
    }
    
    /**
     * Returns Percentage of request that were not already held in the cache.
     * @return Percentage of request that were not already held in the cache.
     */
    @Override
    public double getPercentageMisses() {
        return cache != null ? cache.getCacheMisses() : 0;
    }
    
    /**
     * Returns The number of times the cache has been switched.
     * @return The number of times the cache has been switched.
     */
    @Override
    public long getSwitches() {
        return cache != null ? cache.getCacheSwitches() : 0;
    }

    /**
     * Resets the cache list stats for the list.
     */
    @Override
    public void resetCache() {
        cache.resetCache();
    }
    
    /**
     * Retrieves the entity at the offset or index requested.
     * @param key Index or offset of the entity required.
     * @return A new instance of the entity at the offset or index.
     */
    @Override
    public T get(int key) {
        T item = null;
        try {
            item = cache.active.get(key);
            if (item == null) {
                item = super.get(key);
                cache.active.put(key, item);
                cache.incrementMissesByOne();
            }
            cache.addRecent(key, item);
            cache.incrementRequestsByOne();
        } catch (IOException ex) {
            Logger.getLogger(FixedCacheList.class.getName())
                                    .log(Level.SEVERE, null, ex);
        }
        return item;
    }
}
