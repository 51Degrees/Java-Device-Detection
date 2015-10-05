package fiftyone.mobile.detection.entities.stream;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
 * This Source Code Form is ?Incompatible With Secondary Licenses?, as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */
/**
 * Encapsulates a byte array containing the uncompressed data structures 
 * used by the data set.
 */
public class SourceMemory extends SourceBase {
    /**
     * The buffer containing the source data.
     */
    private final byte[] buffer;
    /**
     * Creates the source from the byte array provided.
     * @param buffer Byte array source of the data.
     */
    public SourceMemory(byte[] buffer) {
        this.buffer = buffer;
    }
    /**
     * Creates a new ByteBuffer from the bytes array.
     * @return new ByteBuffer from the bytes array.
     */
    @Override
    public ByteBuffer createStream() {
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return byteBuffer;
    }
}
