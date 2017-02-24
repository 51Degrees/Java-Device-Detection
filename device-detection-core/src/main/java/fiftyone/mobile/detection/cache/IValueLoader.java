/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
package fiftyone.mobile.detection.cache;

import java.io.IOException;

/**
 * Interface provides methods to load a value for a key
 * <p>
 * This method should not be called as it is part of the internal logic.
 * 
 * @param <K> Type of key
 * @param <V> Type of value
 */
public interface IValueLoader<K,V> {
    /**
     * Returns the value associated with the key.
     * 
     * @param key for the value required
     * @return Value associated with the key
     * @throws java.io.IOException if there was an IOException in the course of loading.
     */
    V load(K key) throws IOException;
}
