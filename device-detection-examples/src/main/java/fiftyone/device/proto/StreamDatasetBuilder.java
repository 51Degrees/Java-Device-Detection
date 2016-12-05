package fiftyone.device.proto;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.factories.StreamFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.EnumMap;

/**
 * Builds a dataset for stream use
 *
 * @author jo
 */
public class StreamDatasetBuilder {
    public enum CacheType {NodeCache, PropertyCache, SignatureCache, StringsCache}

    private static final Date DATE_NONE = new Date(0);
    private boolean isTempFile = false;
    private Date lastModified = DATE_NONE;
    private EnumMap<CacheType, Cache> cacheMap;

    public StreamDatasetBuilder addCache(CacheType cacheType, javax.cache.Cache jsr107Cache) {
        cacheMap.put(cacheType, new Cache.Jsr107Adaptor(jsr107Cache));
        return this;
    }

    public StreamDatasetBuilder addCache(CacheType cacheType, Cache cache) {
        cacheMap.put(cacheType, cache);
        return this;
    }

    public StreamDatasetBuilder addCache(CacheType cacheType, com.google.common.cache.Cache guavaCache) {
        cacheMap.put(cacheType, new Cache.GuavaAdaptor(guavaCache));
        return this;
    }

    public StreamDatasetBuilder isTempfile() {
        isTempFile = true;
        return this;
    }

    public StreamDatasetBuilder lastModified(Date date) {
        lastModified = date;
        return this;
    }

    public Dataset build(String filename) throws IOException {
        Date modDate = lastModified;
        if (modDate.equals(DATE_NONE)) {
            modDate = new Date (new File(filename).lastModified());
        }
        return StreamFactory.create(filename, modDate, isTempFile);
    }
}
