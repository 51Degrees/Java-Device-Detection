package fiftyone.mobile.detection.entities.stream;

import fiftyone.mobile.detection.cache.ICache;
import fiftyone.mobile.detection.entities.Modes;
import fiftyone.mobile.detection.readers.SourceBase;
import fiftyone.mobile.detection.readers.SourceFile;
import fiftyone.mobile.detection.readers.SourceMemory;
import fiftyone.properties.CacheConstants;

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
public class StreamDataset extends fiftyone.mobile.detection.Dataset {

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
    public StreamDataset(String fileName, Date lastModified,
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
    public StreamDataset(byte[] data, Modes mode) throws IOException {
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

    private java.util.Map<CacheConstants.CacheType, ICache> cacheMap = new HashMap<CacheConstants.CacheType, ICache>(5);
    /**
     * Returns a cache to allow examination of its performance
     *
     * @param cacheType the type of cache
     * @return a cache or null if no cache in operation
     */
    public ICache getCache(CacheConstants.CacheType cacheType) {
        return cacheMap.get(cacheType);
    }

    /**
     * Sets the caches to use in this dataset
     *
     * @param cacheMap a Map of caches to use
     */
    public void setCacheMap(java.util.Map<CacheConstants.CacheType, ICache> cacheMap) {
        this.cacheMap = cacheMap;
    }


}
