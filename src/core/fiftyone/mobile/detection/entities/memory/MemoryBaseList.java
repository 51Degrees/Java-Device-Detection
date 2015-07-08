package fiftyone.mobile.detection.entities.memory;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.IDisposable;
import fiftyone.mobile.detection.entities.BaseEntity;
import fiftyone.mobile.detection.entities.IEnumerable;
import fiftyone.mobile.detection.entities.headers.Header;
import fiftyone.mobile.detection.factories.BaseEntityFactory;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
 * Lists can be stored as a set of related objects entirely within memory, or
 * the relevant objects loaded as required from a file or other permanent store
 * as required. 
 * 
 * This class provides base functions for lists implemented in
 * memory using arrays of type T. 
 * 
 * Interfaces are used to create new instances of items to add to the list in 
 * order to avoid creating many inherited list classes for each type. 
 * 
 * The data is held in the private variable array. 
 * 
 * Should not be referenced directly.
 *
 * @param <T> The type the list will contain.
 */
public abstract class MemoryBaseList<T extends BaseEntity> implements
                                            IEnumerable<T>, IDisposable {

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
     * @param dataSet Dataset being created
     * @param reader Reader used to initialise the header
     * @param entityFactory Interface used to create new entities of type T from
     * the read method
     */
    MemoryBaseList(Dataset dataSet, BinaryReader reader,
                                    BaseEntityFactory<T> entityFactory) {
        this.dataSet = dataSet;
        this.entityFactory = entityFactory;
        this.header = new Header(reader);
        this.array = new ArrayList<T>(header.getCount());
    }

    /**
     * Dispose of any items the list holds open.
     */
    @Override
    public void dispose() {
        array.clear();
    }

    /**
     * Returns An iterator to the array of items.
     * @return An iterator to the array of items.
     */
    @Override
    public Iterator<T> iterator() {
        return array.iterator();
    }

    /**
     * Returns The number of entities the list contains.
     * @return The number of entities the list contains.
     */
    public int size() {
        return array.size();
    }
    
    /**
     * Reads all the records to be added to the list.
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     */
    public abstract void read(BinaryReader reader);
}
