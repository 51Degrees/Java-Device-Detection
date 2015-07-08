package fiftyone.mobile.detection.entities.memory;

import fiftyone.mobile.detection.entities.BaseEntity;
import java.util.Iterator;

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
 * used to iterate over the MemoryFixedList.
 * @param <T> The type of BaseEntity the list will contain
 */
public class MemoryFixedListIterator<T extends BaseEntity> 
                                                implements Iterator<T> {
    /**
     * List to iterate over.
     */
    private final MemoryFixedList<T> list;
    /**
     * The number of elements that can be iterated over.
     */
    private final int max;
    /**
     * Current element.
     */
    private int current;
    
    /**
     * Constructs the MemoryFixedListIterator for a given range.
     * @param list is the MemoryFixedList to iterate over.
     * @param start at what index to start.
     * @param finish at what index to finish.
     */
    MemoryFixedListIterator(MemoryFixedList<T> list, int start, int finish) {
        this.list = list;
        this.current = start;
        this.max = finish;
    }
    
    /**
     * Constructs the MemoryFixedListIterator.
     * @param list is the MemoryFixedList to iterate over.
     */
    MemoryFixedListIterator(MemoryFixedList<T> list) {
        this.list = list;
        this.max = list.size();
        this.current = 0;
    }

    /**
     * Check if there are more entities to iterate over.
     * @return true if there are more entities to iterate, false otherwise.
     */
    @Override
    public boolean hasNext() {
        return current < max;
    }

    /**
     * Returns the next entity for retrieval and increments the iteration.
     * @return the next entity for retrieval and increments the iteration.
     */
    @Override
    public T next() {
        try {
            T t = list.get(current);
            current++;
            return t;
        }
        catch(Exception ex) {
            return null;
        }
    }

    /**
     * Not supported.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported.");
    }
    
}
