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
 * This Source Code Form is “Incompatible With Secondary Licenses”, as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */

package fiftyone.mobile.detection;

import java.util.List;

/**
 * Provides the ability to efficiently retrieve the items from the list using 
 * a ranged enumerable. This list can be used with types that are returned from 
 * the BinaryReader implementation where a factory is not required to construct 
 * the entity.
 */
public interface ISimpleList {
    
    /**
     * Returns the values in the list starting at the index provided.
     * 
     * @param index first index of the range required.
     * @param count number of elements to return.
     * @return A list of the items in the range requested.
     */
    public List<Integer> getRange(int index, int count);
    
    /**
     * Returns the value in the list at the index provided.
     * 
     * @param index of the value required.
     * @return Value at the index requested.
     */
    public int get(int index);
    
    /**
     * @return the number of items in the list.
     */
    public int size();
}
