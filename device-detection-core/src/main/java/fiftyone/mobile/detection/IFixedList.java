/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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

import java.io.IOException;

import fiftyone.mobile.detection.entities.BaseEntity;
import java.util.Iterator;

/**
 * Provides the ability to efficiently retrieve the items from the list using 
 * a ranged enumerable.
 * 
 * @param <T> Type of entity the list contains.
 */
public interface IFixedList<T extends BaseEntity> extends IReadonlyList<T> {
    /**
     * Returns an enumerable starting at the index provided until count number 
     * of iterations have been performed.
     * @param index Start index in the fixed list.
     * @param count Number of iterations to perform.
     * @return An enumerable to iterate over the range specified.
     * @throws java.io.IOException
     */
    public abstract IClosableIterator<T> getRange(int index, int count) 
                                                            throws IOException;
}
