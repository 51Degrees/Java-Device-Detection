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
package fiftyone.mobile.detection.entities.headers;

import fiftyone.mobile.detection.readers.BinaryReader;

/**
 * Every list contains a standard initial header. This class provides the basic
 * properties needed to access lists irrespective of the storage implementation.
 * Contains metadata for the associated lists.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 */
public class Header {

    /**
     * Constructs a new instance of Header
     *
     * @param reader Reader connected to the source data structure and
     * positioned to start reading.
     */
    public Header(BinaryReader reader) {
        startPosition = reader.readInt32();
        length = reader.readInt32();
        count = reader.readInt32();
    }

    /**
     * @return the number of items contain in the collection.
     */
    public int getCount() {
        return count;
    }
    private final int count;

    /**
     * @return the position in the file where the data structure starts.
     */
    public int getStartPosition() {
        return startPosition;
    }
    private final int startPosition;

    /**
     * @return the number of bytes consumed by the data structure.
     */
    public int getLength() {
        return length;
    }
    private final int length;
}
