package fiftyone.mobile.detection.entities.stream;

import fiftyone.mobile.detection.entities.BaseEntity;
import java.io.IOException;
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
 * A general class that iterates over entities in StreamVariableLists. The iteration
 * lazy loads for low memory and quick start retrieval.
 */
public class StreamFixedListIterator<T extends BaseEntity> implements Iterator<T> {

    int size;
    
    StreamFixedList<T> fixedList;
    
    int index;
    
    /**
     * Constructs the StreamFixedListIterator.
     * 
     * @param streamFixedList 
     */
    public StreamFixedListIterator(StreamFixedList<T> streamFixedList)
    {
        fixedList = streamFixedList;
        size = streamFixedList.size();
        
        index = 0;
    }
    
    /**
     * Gets if there are any more entities in the list.
     * 
     * @return true if there are more entities to iterate.
     */
    public boolean hasNext() {
    
        return index < size;
    }

    /**
     * Gets the next entity for retrieval and increments the iteration.
     * 
     * @return the next entity in the list.
     */
    public T next() {
        try {
            T t = fixedList.get(index);
            index++;
            return t;
        }
        catch(Exception ex) {
            return null;
        }
    }

    /**
     * remove is not supported.
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
