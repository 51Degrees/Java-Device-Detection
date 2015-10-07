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

import fiftyone.mobile.detection.IDisposable;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * As multiple threads need to read from the Source concurrently this class
 * provides a mechanism for readers to be recycled across threads and requests.
 * 
 * Used by the BaseList of type T to provide multiple readers for the list.
 * 
 * The DetectorDataSet must be disposed of to ensure the readers in the pool 
 * are closed.
 */
public class Pool implements IDisposable {

    /**
     * List of readers available to be used.
     */
    private final Queue<BinaryReader> readers = new LinkedBlockingDeque<BinaryReader>();
    /**
     * A pool of file readers to use to read data from the file.
     */
    private final SourceBase source;
    /**
     * The number of readers that have been created. May not be the same as 
     * the readers in the queue as some may be in use.
     */
    private final AtomicInteger readerCount = new AtomicInteger(0);

    /**
     * Constructs a new pool of readers for the SourceBase provided.
     * @param source The data source for the list
     */
    Pool(SourceBase source) {
        this.source = source;
    }

    /**
     * Returns a reader to the temp file for exclusive use. Release method must
     * be called to return the reader to the pool when finished.
     * @return Reader open and ready to read from the temp file
     * @throws java.io.IOException
     */
    public BinaryReader getReader() throws IOException {
        synchronized(readers) {
            if (readers.isEmpty() == false) {
                return readers.poll();
            }
        }
        
        // There are no readers available so create one
        // and ensure that the reader count is incremented
        // after doing so.
        readerCount.incrementAndGet();
        return source.createReader();
    }

    /**
     * Returns the reader to the pool to be used by another process later.
     * @param reader Reader open and ready to read from the temp file
     */
    public void release(BinaryReader reader) {
        synchronized(readers) {
            readers.add(reader);
        }
    }

    /**
     * Disposes of the source ensuring all the readers are also closed.
     */
    @Override
    public void dispose() {
        readers.clear();
        source.dispose();
    }
    
    /**
     * The number of readers that have been created. May not be the same as 
     * the readers in the queue as some may be in use.
     * @return The number of readers that have been created.
     */
    public int getReadersCreated() {
        synchronized(readers) {
            return readerCount.intValue();
        }
    }
    
    /**
     * Returns The number of readers in the queue.
     * @return The number of readers in the queue.
     */
    public int getReadersQueued() {
        synchronized(readers) {
            return readers.size();
        }
    }
}
