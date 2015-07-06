package fiftyone.mobile.detection.entities.stream;

import fiftyone.mobile.detection.entities.BaseEntity;
import fiftyone.mobile.detection.entities.IEnumerable;
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
 * Class implements the logic of IEnumerable in C#. When you need to read a 
 * specific number of integer objects from the data file it's best to do so 
 * on the as-needed basis instead of loading everything in to memory. This class 
 * provides a way to read the integers one by one.
 */
public class StreamEnumerable<T> implements IEnumerable<T> {
    /**
     * Reader set to the position at the start of the list.
     */
    private final BinaryReader reader;
    /**
     * The number of integers to read to form the array.
     */
    private final int max;
    /**
     * 
     */
    private final Dataset dataSet;
    /**
     * 
     */
    private final BaseEntityFactory<T> entityFactory;
    /**
     * Current position.
     */
    private int current;

    /**
     * An enumerable that can be used to read through the entries.
     * @param reader Reader set to the position at the start of the list.
     * @param max 
     * @param entityFactory 
     */
    public StreamEnumerable(BinaryReader reader, int max, 
            BaseEntityFactory<T> entityFactory, Dataset dataSet, int start) {
        this.dataSet = dataSet;
        this.entityFactory = entityFactory;
        this.current = start;
        this.reader = reader;
        this.max = max;
    }

    /**
     * Returns true if there are more values to read, false otherwise.
     * @return true if there are more values to read, false otherwise.
     */
    @Override
    public boolean hasNext() {
        return current < max;
    }

    /**
     * Reads the next integer and returns it.
     * @return the next integer in a sequence. Null if current > max.
     */
    @Override
    public T next() {
        T result = null;
        try {
            //T result;
            result = (T)entityFactory.create(dataSet, current, reader);
            current++;
        } catch (IOException ex) {
            Logger.getLogger(StreamEnumerable.class.getName())
                                            .log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * Not supported. Do not use.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported.");
        //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Returns this object.
     * @return this object.
     */
    @Override
    public Iterator<T> iterator() {
        return this;
    }
    
    /**
     * Return reader to the pool.
     */
    public void dispose() {
        dataSet.pool.release(reader);
    }
    
    /**
     * When garbage collected, make sure the reader is returned to the pool.
     * @throws Throwable 
     */
    @Override
    protected void finalize() throws Throwable {
        dispose();
        super.finalize();
    }
}
