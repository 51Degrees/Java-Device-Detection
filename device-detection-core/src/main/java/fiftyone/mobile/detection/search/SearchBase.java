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
 * 
 * @param <T> type of items held in the list
 * @param <K> type of the keys used to search the list
 * @param <L> type of the list containing the items
 */
public abstract class SearchBase<T, K, L> {

    /**
     * Runs binary search for the entire list.
     * 
     * @param list list ordered in ascending key value.
     * @param key key to be found in the list.
     * @return the index of the key, or ones complement if not found.
     * @throws IOException if there was a problem accessing data file.
     */
    public int binarySearch(L list, K key) throws IOException {
        return binarySearch(list, key, 0, getCount(list) - 1);
    }
    
    /**
     * Core implementation of the binary search. Runs binary search for a 
     * 
     * @param list list ordered in ascending key value
     * @param key to be found in the list
     * @param lower start search from this index.
     * @param upper search up to and including this index.
     * @return the index of the key, or ones complement if not found.
     * @throws IOException if there was a problem accessing data file.
     */
    public int binarySearch(L list, K key, int lower, int upper) 
                                                            throws IOException {
        int index;

        while (lower <= upper) {
            index = lower + (upper - lower) / 2;
            int comparisonResult = compareTo(getValue(list, index), key);
            if (comparisonResult == 0) {
                return index;
            } else if (comparisonResult > 0) {
                upper = index - 1;
            } else {
                lower = index + 1;
            }
        }
        return ~lower;
    }
    
    public SearchResult binarySearchResults(L list, K key) throws IOException {
        SearchResult results = new SearchResult();
        int lower = 0;
        int upper = getCount(list) - 1;

        while (lower <= upper) {
            results.Iterations++;
            results.Index = lower + (upper - lower) / 2;
            int comparisonResult = compareTo(getValue(list, results.Index), key);
            if (comparisonResult == 0) {
                return results;
            } else if (comparisonResult > 0) {
                upper = results.Index - 1;
            } else {
                lower = results.Index + 1;
            }
        }

        results.Index = ~lower;
        return results;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Abstract methods.">
    /**
     * Gets the count of items in the list.
     * 
     * @param list whose is to be returned
     * @return the number of elements in the list
     */
    protected abstract int getCount(L list);

    /**
     * Used to access list values by index in the list.
     * 
     * @param list whose value is to be returned
     * @param index of the value to return
     * @return the value from the list at the index provided.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    protected abstract T getValue(L list, int index) throws IOException;
    
    /**
     * Compares the key to the item.
     * 
     * @param item to be compared against the key.
     * @param key to compare to the item.
     * @return integer difference.
     * @throws IOException if there was a problem accessing data file.
     */
    protected abstract int compareTo(T item, K key) throws IOException;
    // </editor-fold>
}
