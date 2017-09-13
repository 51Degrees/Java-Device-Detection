/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited.
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
 * Caversham, Reading, Berkshire, United Kingdom RG4 7BY
 *
 * This Source Code Form is the subject of the following patents and patent
 * applications, owned by 51Degrees Mobile Experts Limited of 5 Charlotte
 * Close, Caversham, Reading, Berkshire, United Kingdom RG4 7BY:
 * European Patent No. 2871816;
 * European Patent Application No. 17184134.9;
 * United States Patent Nos. 9,332,086 and 9,350,823; and
 * United States Patent Application No. 15/686,066.
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
package fiftyone.mobile.detection;

import fiftyone.mobile.detection.DatasetBuilder.CacheType;
import fiftyone.mobile.detection.cache.ICache;
import fiftyone.mobile.detection.entities.Modes;
import fiftyone.mobile.detection.entities.stream.Pool;
import fiftyone.mobile.detection.readers.SourceBase;
import fiftyone.mobile.detection.readers.SourceFile;
import fiftyone.mobile.detection.readers.SourceMemory;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

/**
 * A data set which includes a pool of
 * data readers that are used to fetch data from the source when the data
 * set is used to retrieve data not already in memory. It also provides for
 * caching of values.
 * <p>
 * Class provides extra methods to assess status of readers and to
 * evaluate the caches.
 */
public class IndirectDataset extends fiftyone.mobile.detection.Dataset {

    /**
     * Creates a dataset object with a pool of readers used to retrieve data
     * from the data file. Only useful in stram mode.
     *
     * @param lastModified Date and time the source data was last modified.
     * @param fileName Valid path to the uncompressed data set file.
     * @param mode Mode The mode of operation the data set will be using.
     * @param isTempFile True if the file should be deleted when the source is
     * disposed
     * @throws IOException if there was a problem accessing data file.
     */
    public IndirectDataset(String fileName, Date lastModified,
                           Modes mode, boolean isTempFile) throws IOException {
        super(lastModified, mode);
        source = new SourceFile(fileName, isTempFile);
        this.pool = new Pool(source);
    }

    /**
     * Creates a dataset object with a pool of readers used to retrieve data
     * from the data file represented as an array of bytes. Only useful in
     * stram mode.
     *
     * @param data array of bytes to read from.
     * @param mode The mode of operation the data set will be using.
     * @throws IOException if there was a problem accessing data file.
     */
    public IndirectDataset(byte[] data, Modes mode) throws IOException {
        super(new Date(Long.MIN_VALUE), mode);
        source = new SourceMemory(data);
        this.pool = new Pool(source);
    }

    /**
     * A pool of data readers.
     */
    public final Pool pool;
    /**
     * Data source to be used with the pool.
     */
    protected final SourceBase source;

    /**
     * Dispose of the dataset and the pool of readers.
     *
     * @throws IOException if there was a problem accessing data file.
     */
    @Override
    public void close() throws IOException {
        pool.close();
        source.close();
        super.close();
    }

    /**
     * @return The number of readers that have been created in the pool
     * that connects the data set to the data source.
     */
    public int getReadersCreated()
    {
        return pool.getReadersCreated();
    }

    /**
     * @return The number of readers in the queue ready to be used.
     */
    public int getReadersQueued()
    {
        return pool.getReadersQueued();
    }

    /**
     * Resets the caches for the data set. This is a "best efforts"
     * operation that may not be supported by all underlying cache
     * implementations.
     */
    @Override
    public void resetCache() {
        for (ICache cache: cacheMap.values()) {
            if (cache != null) {
                cache.resetCache();
            }
        }
    }

    private java.util.Map<CacheType, ICache> cacheMap = new HashMap<CacheType, ICache>(5);
    /**
     * Returns a cache to allow examination of its performance
     *
     * @param cacheType the type of cache
     * @return a cache or null if no cache in operation
     */
    public ICache getCache(CacheType cacheType) {
        return cacheMap.get(cacheType);
    }

    /**
     * Sets the caches to use in this dataset
     *
     * @param cacheMap a Map of caches to use
     */
    public void setCacheMap(java.util.Map<CacheType, ICache> cacheMap) {
        this.cacheMap = cacheMap;
    }


}
