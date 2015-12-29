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
 * This Source Code Form is “Incompatible With Secondary Licenses”, as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */

package fiftyone.mobile.detection.entities.memory;

import fiftyone.mobile.detection.ISimpleList;
import fiftyone.mobile.detection.entities.headers.Header;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides a way of storing integers in a list and implements utility methods 
 * such as {@link #getRange(int, int)}.
 */
public class MemoryIntegerList implements ISimpleList {
    
    // Entity header.
    private final Header header;
    // Array of items contained in the list.
    protected final Integer[] array;
    
    /**
     * Constructs a new instance of this class.
     * @param reader BinaryReader connected to the source data structure and 
     *               positioned to start reading.
     */
    public MemoryIntegerList(BinaryReader reader) {
        this.header = new Header(reader);
        this.array = new Integer[this.header.getCount()];
    }
    
    /**
     * Reads the list into memory.
     * 
     * @param reader BinaryReader connected to the source data structure and 
     *               positioned to start reading.
     */
    public void read(BinaryReader reader) {
        for (int i = 0; i < header.getCount(); i++) {
            array[i] = reader.readInt32();
        }
    }

    @Override
    public int get(int index) {
        if (index > array.length || index < 0) {
            throw new ArrayIndexOutOfBoundsException("The requested element is "
                    + "out of bounds for this array.");
        }
        return array[index];
    }
    
    @Override
    public List<Integer> getRange(int index, int count) {
        List<Integer> list = Collections.unmodifiableList(Arrays.asList(array));
        return list.subList(index, index + count);
    }
    
    @Override
    public int size() {
        return array.length;
    }
}
