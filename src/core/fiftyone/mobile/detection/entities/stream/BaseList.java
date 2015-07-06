package fiftyone.mobile.detection.entities.stream;

import fiftyone.mobile.detection.IDisposable;
import fiftyone.mobile.detection.IReadonlyList;
import fiftyone.mobile.detection.entities.BaseEntity;
import fiftyone.mobile.detection.entities.headers.Header;
import fiftyone.mobile.detection.factories.BaseEntityFactory;
import fiftyone.mobile.detection.readers.BinaryReader;
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
 * Lists can be stored as a set of related objects entirely within memory, or
 * the relevant objects loaded as required from a file or other permanent store
 * as required. <p> This class provides core functions needed for lists which
 * load objects as required. It implements the Cache of type T to store frequently
 * requested objects and improve memory usage and performance. <p> Interface
 * implementations are used to create new instances of items to add to the list
 * in order to avoid creating many inherited list classes for each BaseEntity
 * type. <p> Should not be referenced directly.
 *
 * @param <T> The type of BaseEntity the list will contain
 */
public abstract class BaseList<T extends BaseEntity> implements
        IReadonlyList<T>, ICacheList, IDisposable {
    /**
     * The dataset which contains the list.
     */
    protected final Dataset dataSet;
    /**
     * Information about the data structure the list is associated with.
     */
    protected final Header header;
    /**
     * Entity factory.
     */
    final BaseEntityFactory<T> entityFactory;

    protected abstract T createEntity(int offset, BinaryReader reader) throws IOException;

    /**
     * Constructs a new instance of BaseList of type T ready to read entities from the
     * source.
     *
     * @param dataSet Dataset being created
     * @param reader Reader used to initialise the header only
     * @param entityFactory a base entity factory to be used
     */
    public BaseList(Dataset dataSet, BinaryReader reader, 
            BaseEntityFactory<T> entityFactory) {
        this.dataSet = dataSet;
        this.header = new Header(reader);
        this.entityFactory = entityFactory;
    }

    /**
     * Retrieves the record at the offset or index requested
     *
     * @param key Index or offset of the record required
     * @return A new instance of the item at the offset or index
     * @throws java.io.IOException
     */
    @Override
    public T get(int key) throws IOException {
        T item = null;
        BinaryReader reader = null;
        try {
            reader = dataSet.pool.getReader();
            item = createEntity(key, reader);
        } catch (Exception e) {
            
        } finally {
            if (reader != null)
                dataSet.pool.release(reader);
        }
        return item;
    }

    /**
     * Disposes of the pool of readers.
     */
    @Override
    public void dispose() {
        dataSet.pool.dispose();
    }

    @Override
    public int size() {
        return header.getCount();
    }
}
