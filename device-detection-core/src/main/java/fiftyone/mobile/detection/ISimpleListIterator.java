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

package fiftyone.mobile.detection;

import java.util.Iterator;
import java.util.List;

/**
 * Implements an Iterator for a provided ArrayRange. Iterating through a 
 * range of array entries is generally more beneficial than making a copy 
 * of the said array.
 * <p>
 * This class should not be referenced directly as it is part of the 
 * internal logic.
 * 
 * @param <T> type of data to hold in the array.
 */
public class ISimpleListIterator<T> implements Iterator{

    private int current;
    private final int max;
    private final List<T> list;
    
    ISimpleListIterator(int max, int current, List<T> list) {
        this.current = current;
        this.max = max;
        this.list = list;
    }

    @Override
    public boolean hasNext() {
        return current < max;
    }

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

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
