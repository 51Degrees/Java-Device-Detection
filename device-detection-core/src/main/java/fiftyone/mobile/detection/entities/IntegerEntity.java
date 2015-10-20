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
 * An integer item in a list of integers.
 */
public class IntegerEntity extends BaseEntity {
    /**
     * The index of the signature in the list of signatures.
     */
    public int value;
    
    /**
     * Constructs a new instance of Integer.
     * @param dataSet data set whose strings list the string is contained within.
     * @param offsetOrIndex The index in the data structure to the integer.
     * @param reader Binary reader positioned at the start of the Integer.
     */
    public IntegerEntity(Dataset dataSet, int offsetOrIndex, BinaryReader reader) {
        super(dataSet, offsetOrIndex);
        value = reader.readInt32();
    }
    
    /**
     * The index of the signature in the list of signatures.
     * @return index of the signature in the list of signatures.
     */
    public int getValue() {
        return value;
    }
}
