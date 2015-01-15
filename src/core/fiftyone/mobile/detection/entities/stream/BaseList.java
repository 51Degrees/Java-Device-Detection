package fiftyone.mobile.detection.entities.stream;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.Disposable;
import fiftyone.mobile.detection.ReadonlyList;
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
        ReadonlyList<T>, ICacheList, Disposable {

    /**
     * Used to store previously accessed items to improve performance and reduce
     * memory consumption associated with creating new instances of entities
     * already in use.
     */
    public final Cache<T> cache;
    /**
     * Pools of binary readers connected to the data source.
     */
    final Pool pool;
    /**
     * The dataset which contains the list.
     */
    protected final Dataset dataSet;
    /**
     * Information about the data structure the list is associated with.
     */
    final Header header;
    final BaseEntityFactory<T> entityFactory;

    protected abstract T createEntity(int offset, BinaryReader reader) throws IOException;

    @Override
    public double getPercentageMisses() {
        return cache.getPercentageMisses();
    }

    public int getCount() {
        return header.getCount();
    }

    /**
     * Constructs a new instance of BaseList of type T ready to read entities from the
     * source.
     *
     * @param dataSet Dataset being created
     * @param reader Reader used to initialise the header only
     * @param source Source data file containing the entire data structure
     * @param entityFactory a base entity factory to be used
     */
    public BaseList(Dataset dataSet, BinaryReader reader, Source source, BaseEntityFactory<T> entityFactory) {
        this.dataSet = dataSet;
        this.pool = new Pool(source);
        this.header = new Header(reader);
        this.cache = new Cache<T>(entityFactory);
        this.entityFactory = entityFactory;
    }

    /**
     * Retrieves the record at the offset or index requested
     *
     * @param offsetOrIndex Index or offset of the record required
     * @return A new instance of the item at the offset or index
     */
    @Override
    public T get(int offsetOrIndex) throws IOException {
        T item;
        item = cache.itemsActive.get(offsetOrIndex);
        if (item == null) {
            BinaryReader reader = pool.getReader();
            item = createEntity(offsetOrIndex, reader);
            pool.release(reader);
            // if we get a collision in here, doesn't really matter - better
            // a collision here than having each read queued
            cache.itemsActive.put(offsetOrIndex, item);
            cache.misses.incrementAndGet();
        }
        cache.addRecent(offsetOrIndex, item);
        cache.requests.incrementAndGet();
        return item;
    }

    /**
     * Disposes of the pool of readers.
     */
    @Override
    public void dispose() {
        cache.dispose();
        pool.dispose();
    }

    @Override
    public int size() {
        return header.getCount();
    }
}
