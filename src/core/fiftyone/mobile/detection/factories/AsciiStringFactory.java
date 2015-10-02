package fiftyone.mobile.detection.factories;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.AsciiString;
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
public class AsciiStringFactory extends BaseEntityFactory<AsciiString> {

    /**
     * Creates a new instance of AsciiString
     *
     * @param dataSet The data set whose strings list the string is contained
     * within
     * @param offset The offset to the start of the string within the string
     * data structure
     * @param reader Binary reader positioned at the start of the AsciiString
     * @return A new instance of an AsciiString
     */
    public AsciiString create(Dataset dataSet, int offset,
            BinaryReader reader) {
        return new AsciiString(dataSet, offset, reader);
    }

    /**
     * Returns the length of the AsciiString entity including the 2 bytes for
     * the length and the null terminator not used by java.
     *
     * @param entity Entity of type AsciiString
     * @return Length in bytes of the AsciiString
     */
    @Override
    public int getLength(AsciiString entity) {
        return entity.getLength() + 3;
    }
}