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
package fiftyone.mobile.detection.entities;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.readers.BinaryReader;

/**
 * ASCII format strings are the only ones used in the data set.
 * <p>
 * Many native string formats use UNICODE format using 2 bytes for every 
 * character. This is inefficient when only ASCII values are being stored. 
 * The AsciiString class wraps a byte array of ASCII characters and exposes 
 * them as a native string type when required. 
 * <p> 
 * Strings stored as ASCII strings include, the relevant characters from 
 * signatures, sub strings longer than 4 characters, property and value names, 
 * the descriptions and URLs associated with properties and values. 
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 * <p>
 * For more information see: 
 * <a href="https://51degrees.com/support/documentation/device-detection-data-model">
 * 51Degrees pattern data model</a>.
 */
public class AsciiString extends BaseEntity {

    /**
     * The value of the string in ASCII bytes.
     */
    final byte[] value;

    /**
     * The length of the byte array or string in characters.
     * 
     * @return length of the byte array or string in characters.
     */
    public int getLength() {
        return value.length;
    }

    /**
     * Constructs a new instance of AsciiString.
     *
     * @param dataSet The data set whose strings list the string is contained
     * within.
     * @param offset The offset to the start of the string within the string
     * data structure.
     * @param reader Reader connected to the source data structure and
     * positioned to start reading.
     */
    public AsciiString(Dataset dataSet, int offset, BinaryReader reader) {
        super(dataSet, offset);

        // The length includes the null byte which we're not interested in.
        this.value = reader.readBytes(reader.readInt16() - 1);

        // Read the null byte to ensure the file position is at the 
        // expected place for the next string.
        reader.readByte();
        
        this.stringValue = null;
    }

    /**
     * .NET string representation of the ASCII string.
     *
     * @return ASCII string as a native string.
     */
    @Override
    @SuppressWarnings("DoubleCheckedLocking")
    public String toString() {
        String localStringValue = stringValue;
        if (localStringValue == null) {
            synchronized (this) {
                localStringValue = stringValue;
                if (localStringValue == null) {
                    stringValue = localStringValue = new String(value);
                }
            }
        }
        return localStringValue;
    }
    private volatile String stringValue;
}
