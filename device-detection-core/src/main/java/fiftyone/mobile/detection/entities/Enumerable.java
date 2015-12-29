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

import fiftyone.mobile.detection.readers.BinaryReader;
import java.util.Iterator;

/**
 * Class implements the logic of IEnumerable in C#. 
 * <p>
 * When you need to read a specific number of integer objects from the data 
 * file it's best to do so on the as-needed basis instead of loading 
 * everything in to memory. This class provides a way to read the integers 
 * one by one.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 * <p>
 * For more information see: 
 * <a href="https://51degrees.com/support/documentation/device-detection-data-model">
 * 51Degrees pattern data model</a>.
 */
public class Enumerable implements Iterator<Integer>, Iterable<Integer> {
    /**
     * Reader set to the position at the start of the list.
     */
    private final BinaryReader reader;
    /**
     * The number of integers to read to form the array.
     */
    int max;
    /**
     * Current position.
     */
    int count;

    /**
     * An enumerable that can be used to read through the entries.
     * 
     * @param reader Reader set to the position at the start of the list.
     * @param max number of elements that can possibly be returned.
     */
    public Enumerable(BinaryReader reader, int max) {
        this.count = 0;
        this.reader = reader;
        this.max = max;
    }

    /**
     * Get the next element.
     * 
     * @return integer entry.
     */
    public int getNext() {
        Integer value = null;
        if (count < max) {
            value = reader.readInt32();
            count++;
            return value;
        }
        return value;
    }

    /**
     * Returns true if there are more values to read, false otherwise.
     * 
     * @return true if there are more values to read, false otherwise.
     */
    @Override
    public boolean hasNext() {
        return count < max;
    }

    /**
     * Reads the next integer and returns it.
     * 
     * @return the next integer in a sequence. Null if current greater than max.
     */
    @Override
    public Integer next() {
        return getNext();
    }

    /**
     * Not supported.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    /**
     * Returns this object.
     * 
     * @return this object.
     */
    @Override
    public Iterator<Integer> iterator() {
        return this;
    }
    
}
