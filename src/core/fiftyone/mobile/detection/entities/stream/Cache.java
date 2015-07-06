package fiftyone.mobile.detection.entities.stream;

import fiftyone.mobile.detection.entities.BaseEntity;

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
 * Provides an additional method to reduce the number of parameters passed when 
 * adding an item to the cache.
 * 
 * @param <T> The type of BaseEntity the cache will contain.
 */
public class Cache<T extends BaseEntity> 
                        extends fiftyone.mobile.detection.Cache<Integer, T> {

    /**
     * Constructs a new instance of the cache.
     * @param cacheSize number of items in cache.
     */
    public Cache(int cacheSize) {
        super(cacheSize);
    }

    /**
     * Adds a new item to cache.
     * @param index index of the item.
     * @param item item to add.
     */
    public void addRecent(int index, T item) {
        super.addRecent(index, item);
    }
    
    /**
     * Resets the stats for the cache.
     */
    public void resetCache() {
        super.clearActiveList();
        super.clearBackgroundList();
        super.clearMisses();
        super.clearRequests();
        super.clearSwitches();
    }
}
