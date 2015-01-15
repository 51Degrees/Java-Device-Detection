package fiftyone.mobile.detection.entities.stream;

import fiftyone.mobile.detection.Disposable;
import fiftyone.mobile.detection.readers.TrieReader;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

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
 * As multiple threads need to read from the Source concurrently this class
 * provides a mechanism for readers to be recycled across threads and requests.
 * <p> Used by the BaseList of type T to provide multiple readers for the list. <p> The
 * DetectorDataSet must be disposed of to ensure the readers in the pool are
 * closed.
 */
public class TriePool implements Disposable {

    // List of readers available to be used.
    private final Queue<TrieReader> readers = new LinkedBlockingDeque<TrieReader>();
    // A pool of file readers to use to read data from the file.
    private final TrieSource source;

    /**
     * Constructs a new pool of readers for the source provided.
     *
     **** @param source The data source for the list
     */
    public TriePool(TrieSource source) {
        this.source = source;
    }

    /**
     * Returns a reader to the temp file for exclusive use. Release method must
     * be called to return the reader to the pool when finished.
     *
     * @return Reader open and ready to read from the temp file
     * @throws java.io.IOException indicates an I/O exception occurred
     */
    public TrieReader getReader() throws IOException {
        TrieReader reader = readers.poll();
        if (reader == null) {
            reader = source.createReader();
        }
        return reader;
    }

    /**
     * Returns the reader to the pool to be used by another process later.
     *
     **** @param reader Reader open and ready to read from the temp file
     */
    public void release(TrieReader reader) {
        readers.add(reader);
    }

    @Override
    public void dispose() {
        source.dispose();
    }
}
