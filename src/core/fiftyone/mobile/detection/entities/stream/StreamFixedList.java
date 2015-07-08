package fiftyone.mobile.detection.entities.stream;

import fiftyone.mobile.detection.IFixedList;
import fiftyone.mobile.detection.entities.BaseEntity;
import fiftyone.mobile.detection.factories.BaseEntityFactory;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;
import java.util.Iterator;
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
 * Lists can be stored as a set of related objects entirely within memory, or 
 * the relevant objects loaded as required from a file or other permanent store
 * as required.
 * 
 * Delegate methods are used to create new instances of items to add to the list
 * in order to avoid creating many inherited list classes for each type.
 * 
 * Should not be referenced directly.
 *
 * @param <T> The type of BaseEntity the list will contain 
 */
public class StreamFixedList<T extends BaseEntity> extends StreamBaseList<T> 
                                                    implements IFixedList<T> {
    /**
     * Constructs a new instance of StreamBaseList{T} ready to 
     * read entities from the source.
     * @param dataSet Dataset being created.
     * @param reader Reader used to initialise the header only.
     * @param entityFactory Used to create new instances of the entity.
     */
    public StreamFixedList(Dataset dataSet, BinaryReader reader, 
                            BaseEntityFactory<T> entityFactory) {
        super(dataSet, reader, entityFactory);
    }

    /**
     * An enumerator for the list between the range provided.
     * @param index read from.
     * @param count how many entries.
     * @return An enumerator for the list.
     */
    @Override
    public StreamEnumerable<T> getRange(int index, int count) {
        BinaryReader reader = null;
        StreamEnumerable enumerable = null;
        try {
            reader = dataSet.pool.getReader();
            reader.setPos(header.getStartPosition() 
                            + (entityFactory.getLength() * index));
            enumerable = new StreamEnumerable(reader, index, entityFactory, 
                                                dataSet, count);
        } catch (IOException ex) {
            Logger.getLogger(StreamFixedList.class.getName())
                    .log(Level.SEVERE, null, ex);
        } finally {
            if (reader != null)
                dataSet.pool.release(reader);
        }
        return enumerable;
    }

    /**
     * Returns The number of items in the list.
     * @return The number of items in the list.
     */
    @Override
    public int size() {
        return super.size();
    }

    /**
     * Not implemented. Do not use.
     */
    @Override
    public void dispose() {
        throw new UnsupportedOperationException("Not supported yet."); 
        //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Creates a new entity of type T.
     * @param index The index of the entity being created.
     * @param reader Reader connected to the source data structure and
     * positioned to start reading.
     * @return A new entity of type T at the index provided.
     * @throws IOException indicates an I/O exception occurred
     */
    @Override
    protected T createEntity(int index, BinaryReader reader) 
                                                throws IOException {
        reader.setPos(header.getStartPosition() 
                        + (entityFactory.getLength() * index));
        return entityFactory.create(dataSet, index, reader);
    }
    
    /**
     * Returns An enumeration for the underlying list.
     * @return An enumeration for the underlying list.
     */
    public StreamEnumerable getEnumerator() {
        return getRange(0, size());
    }

    /**
     * Not supported.
     * @return nothing.
     */
    @Override
    public Iterator<T> iterator() {
        throw new UnsupportedOperationException("Not supported.");
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Not supported.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
