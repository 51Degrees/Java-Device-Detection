package fiftyone.mobile.detection.entities.stream;

import fiftyone.mobile.detection.entities.Modes;
import java.io.IOException;
import java.util.Date;

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
 * A data set returned from the stream factory which includes a pool of
 * data readers that are used to fetch data from the source when the data 
 * set is used to retrieve data not already in memory.
 */
public class Dataset extends fiftyone.mobile.detection.Dataset {
    /**
     * A pool of data readers.
     */
    public final Pool pool;
    
    /**
     * Creates a dataset object with a pool of readers used to retrieve data 
     * from the data file. Only useful in stram mode.
     * @param lastModified Date and time the source data was last modified.
     * @param fileName Valid path to the uncompressed data set file.
     * @param mode Mode The mode of operation the data set will be using.
     * @throws IOException 
     */
    public Dataset(String fileName, Date lastModified, Modes mode) throws IOException {
        super(lastModified, mode);
        this.pool = new Pool(new SourceFile(fileName, false));
    }
    
    /**
     * Creates a dataset object with a pool of readers used to retrieve data 
     * from the data file represented as an array of bytes. Only useful in 
     * stram mode.
     * @param data array of bytes to read from.
     * @param mode The mode of operation the data set will be using.
     * @throws IOException 
     */
    public Dataset(byte[] data, Modes mode) throws IOException {
        super(new Date(Long.MIN_VALUE), mode);
        this.pool = new Pool(new SourceMemory(data));
    }
    
    /**
     * Dispose of the dataset and the pool of readers.
     */
    @Override
    public void dispose() {
        pool.dispose();
        super.dispose();
    }
    
    /**
     * Resets the cache for the data set.
     */
    @Override
    public void resetCache() {
        super.resetCache();
        ((ICacheList)super.signatures).resetCache();
        ((ICacheList)super.nodes).resetCache();
        ((ICacheList)super.strings).resetCache();
        ((ICacheList)super.profiles).resetCache();
        ((ICacheList)super.values).resetCache();
        ((ICacheList)super.rankedSignatureIndexes).resetCache();
    }
}
