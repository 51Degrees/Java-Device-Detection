package fiftyone.mobile.detection.entities.memory;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.IFixedList;
import fiftyone.mobile.detection.entities.BaseEntity;
import fiftyone.mobile.detection.entities.IEnumerable;
import fiftyone.mobile.detection.factories.BaseEntityFactory;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 * A readonly list of fixed length entity types held in memory. <p> Entities in
 * the underlying data structure are either fixed length where the data that
 * represents them always contains the same number of bytes, or variable length
 * where the number of bytes to represent the entity varies. <p> This class uses
 * the index of the entity in the accessor. The list is typically used by
 * entities that need to be found quickly using a divide and conquer algorithm.
 * <p> The constructor will read the header information about the underlying
 * data structure and the entities are added to the list when the Read method is
 * called. <p> The constructor will read the header information about the
 * underlying data structure and the entities are added to the list when the
 * Read method is called. <p> The class supports source stream that do not
 * support seeking. <p> Should not be referenced directly.
 *
 * @param <T> The type of BaseEntity the list will contain
 *
 */
public class MemoryFixedList<T extends BaseEntity> extends MemoryBaseList<T> 
                                                   implements IFixedList<T>{

    /**
     * Constructs a new instance of MemoryFixedList
     * @param dataSet The DataSet being created
     * @param reader Reader connected to the source data structure and
     * positioned to start reading
     * @param entityFactory Interface implementation used to create new entities
     * of type T
     */
    public MemoryFixedList(Dataset dataSet, BinaryReader reader,
            BaseEntityFactory<T> entityFactory) {
        super(dataSet, reader, entityFactory);
    }

    /**
     * Reads the list into memory
     * @param reader Reader connected to the source data structure and
     * positioned to start reading
     */
    @Override
    public void read(BinaryReader reader) {
        try {
            for (int index = 0; index < header.getCount(); index++) {
                array.add(entityFactory.create(dataSet, index, reader));
            }
        } catch (IOException ex) {
            Logger.getLogger(MemoryFixedList.class.getName())
                                            .log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Accessor for the fixed list
     * @param i The index of the entity to be returned from the list.
     * @return Entity at the index requested.
     */
    @Override
    public T get(int i) {
        return array.get(i);
    }

    /**
     * Not supported.
     * @return nothing.
     */
    @Override
    public boolean hasNext() {
        throw new UnsupportedOperationException("Not supported."); 
    }

    /**
     * Not supported.
     * @return nothing.
     */
    @Override
    public T next() {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * Not supported.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * Returns the MemoryFixedListIterator for specific range.
     * @param index start at this element.
     * @param count finish at this element.
     * @return the MemoryFixedListIterator for specific range.
     */
    @Override
    public IEnumerable<T> getRange(int index, int count) {
        return new MemoryFixedListIterator<T>(this, index, count);
    }
}
