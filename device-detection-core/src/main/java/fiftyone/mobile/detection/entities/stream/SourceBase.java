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

import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Providers the base for a data source containing the uncompressed data 
 * structures used by the data set.
 * 
 * Must be disposed to ensure that the readers are closed and any resources
 * free for other uses.
 */
abstract class SourceBase implements Closeable {

    /**
     * List of binary readers opened against the data source. 
     */
    private final List<BinaryReader> readers = new ArrayList<BinaryReader>();
    
    /**
     * Creates a new reader and stores a reference to it.
     * @return A reader open for read access to the stream
     */
    BinaryReader createReader() throws IOException {
        BinaryReader reader = new BinaryReader(createStream());
        synchronized(this) {
            readers.add(reader);
        }
        return reader;
    }
    
    /**
     * Releases the reference to memory and forces garbage collection.
     * @throws java.io.IOException
     */
    @Override
    public void close() throws IOException {
        synchronized(readers) {
            for (BinaryReader reader : readers) {
                reader.close();
            }
            readers.clear();
        }
    }
    
    /**
     * Creates a new stream from the data source.
     * @return A freshly opened stream to the data source.
     */
    abstract ByteBuffer createStream() throws IOException;
}
