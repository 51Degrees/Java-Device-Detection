package fiftyone.mobile.detection.entities.stream;

import fiftyone.mobile.detection.IDisposable;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

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
 * Providers the base for a data source containing the uncompressed data 
 * structures used by the data set.
 * 
 * Must be disposed to ensure that the readers are closed and any resources
 * free for other uses.
 */
public abstract class SourceBase implements IDisposable {

    /**
     * List of binary readers opened against the data source. 
     */
    private final List<BinaryReader> readers = new ArrayList<BinaryReader>();
    
    /**
     * Creates a new reader and stores a reference to it.
     * @return A reader open for read access to the stream
     */
    public BinaryReader createReader() {
        BinaryReader reader = new BinaryReader(createStream());
        synchronized(this) {
            readers.add(reader);
        }
        return reader;
    }
    
    /**
     * Releases the reference to memory and forces garbage collection.
     */
    @Override
    public void dispose() {
        synchronized(readers) {
            for (BinaryReader br : readers) {
                br.dispose();
            }
            readers.clear();
        }
    }
    
    /**
     * Creates a new stream from the data source.
     * @return A freshly opened stream to the data source.
     */
    public abstract ByteBuffer createStream();
}
