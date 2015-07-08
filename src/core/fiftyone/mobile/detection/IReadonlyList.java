package fiftyone.mobile.detection;

import fiftyone.mobile.detection.entities.BaseEntity;
import fiftyone.mobile.detection.entities.IEnumerable;
import java.io.IOException;

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
 * A list which only provides those features needed to read items from the list.
 * @param <T> The type of BaseEntity the list will contain.
 */
public interface IReadonlyList<T extends BaseEntity> extends IEnumerable<T>,
        IDisposable {
    /**
     * Accessor for the list.
     * @param i Index or offset of the entity required.
     * @return Accessor for the list.
     * @throws IOException 
     */
    T get(int i) throws IOException;

    /**
     * Returns number of items in the list.
     * @return number of items in the list.
     */
    int size();
}
