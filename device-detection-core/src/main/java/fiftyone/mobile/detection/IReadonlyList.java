/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
 * Caversham, Reading, Berkshire, United Kingdom RG4 7BY
 * 
 * This Source Code Form is the subject of the following patents and patent
 * applications, owned by 51Degrees Mobile Experts Limited of 5 Charlotte
 * Close, Caversham, Reading, Berkshire, United Kingdom RG4 7BY: 
 * European Patent No. 2871816;
 * European Patent Application No. 17184134.9;
 * United States Patent Nos. 9,332,086 and 9,350,823; and
 * United States Patent Application No. 15/686,066.
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
import java.io.Closeable;

/**
 * Provides a read-only set of features to access the list. All of the 
 * 51Degrees entities are stored in a read-only lists within the dataset to 
 * prevent elements elements being removed unintentionally.
 * 
 * @param <T> The type of BaseEntity the list will contain.
 */
public interface IReadonlyList<T extends BaseEntity> 
                                                extends Iterable<T>, Closeable {
    /**
     * Accessor for the list.
     * 
     * @param i Index or offset of the entity required.
     * @return Accessor for the list.
     * @throws IOException if there was a problem accessing data file.
     */
    T get(int i) throws IOException;

    /**
     * Returns number of items in the list.
     * 
     * @return number of items in the list.
     */
    int size();
}
