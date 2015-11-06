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
package fiftyone.mobile.detection.factories;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;

public abstract class BaseEntityFactory<T> {

    public abstract T create(Dataset dataSet, int index, BinaryReader reader) 
                                                            throws IOException;

    /**
     * @param entity whose size is required.
     * @return the size of the entity provided.
     * @throws java.io.IOException
     */
    public int getLength(T entity) throws IOException {
        throw new UnsupportedOperationException("Can not retrieve the size of "
                + "the provided entity from an abstract class BaseEntity. This "
                + "method must be implemented in an extending class");
    }

    /**
     * @return returns the size of a fixed length entity type.
     * @throws java.io.IOException
     */
    public int getLength() throws IOException {
        throw new UnsupportedOperationException("Can not retrieve the size of "
                + "the fixed length entity from an abstract class BaseEntity. "
                + "This method must be implemented in an extending class");
    }
}
