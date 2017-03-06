/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
package fiftyone.mobile.detection.readers;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Providers the base for a data source containing the uncompressed data 
 * structures used by the data set.
 * <p>
 * Must be disposed to ensure that the readers are closed and any resources
 * free for other uses.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 */
public abstract class SourceBase implements Closeable {
    
    /**
     * Creates a new reader and stores a reference to it.
     * @return A reader open for read access to the stream
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public BinaryReader createReader() throws IOException {
        return new BinaryReader(createStream());
    }
   
    /**
     * Creates a new stream from the data source.
     * @return A freshly opened stream to the data source.
     */
    abstract ByteBuffer createStream() throws IOException;
}
