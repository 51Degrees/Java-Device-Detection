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
package fiftyone.mobile.detection.entities.stream;

/**
 * Interface provides access to the cache statistics which can be used for 
 * performance tuning purposes. Used in conjunction with lists where items can 
 * be cached.
 */
public interface ICacheList {
    /**
     * Returns the percentage of cache misses.
     * 
     * @return the percentage of cache misses.
     */
    double getPercentageMisses();
    
    /**
     * The number of times the lists have been switched.
     * Note: The LRU does not require switching and this method has been 
     * deprecated.
     * 
     * @return The number of times the lists have been switched.
     */
    @Deprecated
    long getSwitches();
    
    /**
     * Resets the cache used by the list.
     */
    void resetCache();
}