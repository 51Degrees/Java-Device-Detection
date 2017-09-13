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
package fiftyone.mobile.detection.entities.memory;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.IReadonlyList;
import fiftyone.mobile.detection.entities.BaseEntity;
import fiftyone.mobile.detection.entities.headers.Header;
import fiftyone.mobile.detection.factories.BaseEntityFactory;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Lists can be stored as a set of related objects entirely within memory, or
 * the relevant objects loaded as required from a file or other permanent store
 * as required. 
 * <p>
 * This class provides base functions for lists implemented in memory using 
 * arrays of type T. List extended by other lists in this package.
 * <p>
 * Interfaces are used to create new instances of items to add to the list in 
 * order to avoid creating many inherited list classes for each type. 
 * <p>
 * The data is held in the private variable array. 
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 *
 * @param <T> The type the list will contain.
 */
public abstract class MemoryBaseList<T extends BaseEntity> implements
                                            IReadonlyList<T>, Closeable {

    /**
     * Array of items contained in the list.
     */
    protected final List<T> array;
    /**
     * The dataset which contains the list.
     */
    protected final Dataset dataSet;
    /**
     * Interface used to create a new instance of an item in the list.
     */
    protected final BaseEntityFactory<T> entityFactory;
    /**
     * Information about the data structure the list is associated with.
     */
    public final Header header;

    /**
     * Constructs a new instance of <T>. The read method needs to be called
     * following construction to read all the entities which form the list
     * before the list can be used.
     *
     * @param dataSet Dataset being created.
     * @param reader Reader used to initialise the header.
     * @param entityFactory Interface used to create new entities of type T from
     * the read method.
     */
    MemoryBaseList(Dataset dataSet, BinaryReader reader,
                                    BaseEntityFactory<T> entityFactory) {
        this.header = new Header(reader);
        int count = this.header.getCount();
        this.array = new ArrayList<T>(count);
        this.dataSet = dataSet;
        this.entityFactory = entityFactory;
    }

    /**
     * Dispose of any items the list holds open.
     */
    @Override
    public void close() {
        array.clear();
    }

    /**
     * Returns An iterator to the array of items.
     * 
     * @return An iterator to the array of items.
     */
    @Override
    public Iterator<T> iterator() {
        return array.iterator();
    }

    /**
     * Returns The number of entities the list contains.
     * 
     * @return The number of entities the list contains.
     */
    @Override
    public int size() {
        return array.size();
    }
    
    /**
     * Reads all the records to be added to the list.
     * 
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public abstract void read(BinaryReader reader) throws IOException;
}
