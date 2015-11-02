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
package fiftyone.mobile.detection.search;

import java.io.IOException;

/**
 * Used to search ordered lists where the overhead of creating an item to
 * use with the Java binarySearch method is unnecessary. The base class is 
 * generic to support both arrays and lists.
 * @param <T> type of items held in the list
 * @param <K> type of the keys used to search the list
 * @param <L> type of the list containing the items
 */
public abstract class SearchBase<T, K, L> {
    
    /**
     * Gets the count of items in the list.
     * @param list whose is to be returned
     * @return the number of elements in the list
     */
    protected abstract int getCount(L list);

    /**
     * Used to access list values by index in the list.
     * @param list whose value is to be returned
     * @param index of the value to return
     * @return the value from the list at the index provided.
     * @throws java.io.IOException
     */
    protected abstract T getValue(L list, int index) throws IOException;
    
    /**
     * Compares the key to the item.
     * @param item to be compared against the key
     * @param key to compare to the item
     * @return
     * @throws IOException 
     */
    protected abstract int compareTo(T item, K key) throws IOException;
 
    /**
     * Core implementation of the binary search.
     * @param list list ordered in ascending key value
     * @param key to be found in the list
     * @return the index of the key, or ones complement if not found
     * @throws IOException 
     */
    public int binarySearch(L list, K key) throws IOException {
        int lower = 0;
        int upper = getCount(list) - 1;

        while (lower <= upper) {
            int middle = lower + (upper - lower) / 2;
            int comparisonResult = compareTo(getValue(list, middle), key);
            if (comparisonResult == 0) {
                return middle;
            } else if (comparisonResult > 0) {
                upper = middle - 1;
            } else {
                lower = middle + 1;
            }
        }

        return ~lower;
    }
}
