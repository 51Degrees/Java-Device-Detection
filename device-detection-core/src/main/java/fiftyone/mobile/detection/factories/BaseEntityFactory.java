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
package fiftyone.mobile.detection.factories;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;

/**
 * Provides common set of methods for all entity factories.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 * 
 * @param <T> Type of entity to create.
 */
public abstract class BaseEntityFactory<T> {

    /**
     * Creates a new entity.
     * 
     * @param dataSet The data set to read entity from.
     * @param index The offset to the start of the entity within the string
     * data structure.
     * @param reader Binary reader positioned at the start of the AsciiString.
     * @return A new instance of an entity.
     * @throws IOException if there was a problem accessing data file.
     */
    public abstract T create(Dataset dataSet, int index, BinaryReader reader) 
                                                            throws IOException;

    /**
     * @param entity whose size is required.
     * @return the size of the entity provided.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public int getLength(T entity) throws IOException {
        throw new UnsupportedOperationException("Can not retrieve the size of "
                + "the provided entity from an abstract class BaseEntity. This "
                + "method must be implemented in an extending class");
    }

    /**
     * @return returns the size of a fixed length entity type.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public int getLength() throws IOException {
        throw new UnsupportedOperationException("Can not retrieve the size of "
                + "the fixed length entity from an abstract class BaseEntity. "
                + "This method must be implemented in an extending class");
    }
}
