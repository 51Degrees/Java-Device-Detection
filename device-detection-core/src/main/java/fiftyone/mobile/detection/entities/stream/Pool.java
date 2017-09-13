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
package fiftyone.mobile.detection.entities.stream;

import fiftyone.mobile.detection.readers.BinaryReader;
import fiftyone.mobile.detection.readers.SourceBase;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * As multiple threads need to read from the Source concurrently this class
 * provides a mechanism for readers to be recycled across threads and requests.
 * <p>
 * Each data set constructed using the Stream Factory will maintain a pool of 
 * readers in order to perform device lookup and retrieve various entities.
 * <p>
 * Used by the BaseList of type T to provide multiple readers for the list.
 * <p>
 * The Dataset must be disposed of to ensure the readers in the pool 
 * are closed.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 * <p>
 * The concept of the pool is slightly different to that, used in our C API. 
 * The C API also maintains a pool, but it is a pool of work sets where each 
 * work set is constructed from the data set and contains device information 
 * relevant to the device being detected. This pool only contains readers.
 * <p>
 * The number of readers created by the pool will typically be limited to the
 * number of concurrent threads in operation. As such the number of readers
 * will be relatively small and the Pool does not need to limit the maximum
 * number that can be created.
 */
public class Pool implements Closeable {

    /**
     * Linked list of readers available for use.
     */
    private final ConcurrentLinkedQueue<BinaryReader> readers = 
            new ConcurrentLinkedQueue<BinaryReader>();
    
    /**
     * A source of file readers to use to read data from the file.
     */
    private final SourceBase source;
    
    /**
     * The number of readers that have been created. May not be the same as 
     * the readers in the queue as some may be in use.
     */
    private final AtomicInteger readerCount = new AtomicInteger(0);

    /**
     * Constructs a new pool of readers for the SourceBase provided.
     * 
     * @param source The data source for the list.
     */
    public Pool(SourceBase source) {
        this.source = source;
    }

    /**
     * Returns a reader to the temp file for exclusive use. Release method must
     * be called to return the reader to the pool when finished.
     * 
     * @return Reader open and ready to read from the temp file.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public BinaryReader getReader() throws IOException {
        BinaryReader reader = readers.poll();
        
        if (reader == null) {
            // There are no readers available so create one
            // and ensure that the reader count is incremented
            // after doing so.
            readerCount.incrementAndGet();
            reader = source.createReader();
        }
        
        return reader;
    }

    /**
     * Returns the reader to the pool to be used by another process later.
     * @param reader Reader open and ready to read from the temp file
     */
    public void release(BinaryReader reader) {
        readers.add(reader);
    }
   
    /**
     * The number of readers that have been created. May not be the same as 
     * the readers in the queue as some may be in use.
     * 
     * @return The number of readers that have been created.
     */
    public int getReadersCreated() {
        return readerCount.get();
    }
    
    /**
     * Returns The number of readers in the queue.
     * 
     * @return The number of readers in the queue.
     */
    public int getReadersQueued() {
        return readers.size();
    }


    @Override
    public void close() throws IOException {
        while(!readers.isEmpty()) {
            BinaryReader reader = readers.poll();
            if (reader != null) {
                reader.close();
            }
        }
    }
}
