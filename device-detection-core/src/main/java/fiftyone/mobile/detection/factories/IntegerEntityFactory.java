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
package fiftyone.mobile.detection.factories;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.readers.BinaryReader;
import fiftyone.mobile.detection.entities.IntegerEntity;
import java.io.IOException;

/**
 * Creates a new instance of Integer.
 */
public class IntegerEntityFactory extends BaseEntityFactory<IntegerEntity> {

    /**
     * Creates a new instance of Integer.
     * @param dataSet data set whose data structure includes integer values.
     * @param index index to the start of the Integer within the data structure.
     * @param reader Binary reader positioned at the start of the Integer.
     * @return A new instance of an Integer.
     * @throws IOException 
     */
    @Override
    public IntegerEntity create(Dataset dataSet, int index, BinaryReader reader) throws IOException {
        return new IntegerEntity(dataSet, index, reader);
    }
    
    /**
     * Returns the length of the Integer entity
     * @return Length in bytes of the RankedSignatureIndex.
     */
    @Override
    public int getLength() {
        return (java.lang.Integer.SIZE / java.lang.Byte.SIZE);
    }
}
