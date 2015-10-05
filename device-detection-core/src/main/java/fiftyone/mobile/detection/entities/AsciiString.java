package fiftyone.mobile.detection.entities;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.readers.BinaryReader;

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
 * ASCII format strings are the only ones used in the data set. Many native
 * string formats use Unicode format using 2 bytes for every character. This is
 * inefficient when only ASCII values are being stored. The AsciiString class
 * wraps a byte array of ASCII characters and exposes them as a native string
 * type when required. <p> Strings stored as ASCII strings include, the relevant
 * characters from signatures, sub strings longer than 4 characters, property
 * and value names, the descriptions and URLs associated with properties and
 * values. <p> For more information see
 * http://51degrees.mobi/Support/Documentation/Java
 */
public class AsciiString extends BaseEntity {

    /**
     * The value of the string in ASCII bytes.
     */
    final byte[] value;

    /**
     * The length of the byte array or string in characters.
     * @return length of the byte array or string in characters.
     */
    public int getLength() {
        return value.length;
    }

    /**
     * Constructs a new instance of AsciiString
     *
     * @param dataSet The data set whose strings list the string is contained
     * within
     * @param offset The offset to the start of the string within the string
     * data structure
     * @param reader reader to be used
     */
    public AsciiString(Dataset dataSet, int offset, BinaryReader reader) {
        super(dataSet, offset);

        // The length includes the null byte which we're not interested in.
        this.value = reader.readBytes(reader.readInt16() - 1);

        // Read the null byte to ensure the file position is at the 
        // expected place for the next string.
        reader.readByte();
    }

    /**
     * .NET string representation of the ASCII string.
     *
     * @return ASCII string as a native string.
     */
    @Override
    public String toString() {
        if (stringValue == null) {
            synchronized (this) {
                if (stringValue == null) {
                    stringValue = new String(value);
                }
            }
        }
        return stringValue;
    }
    private String stringValue = null;
}
