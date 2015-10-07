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
package fiftyone.mobile.detection.entities.stream;

import fiftyone.mobile.detection.IDisposable;
import fiftyone.mobile.detection.IDisposableIterator;
import fiftyone.mobile.detection.entities.BaseEntity;
import fiftyone.mobile.detection.factories.BaseEntityFactory;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class implements the logic of IEnumerable in C#. When you need to read a 
 * specific number of integer objects from the data file it's best to do so 
 * on the as-needed basis instead of loading everything in to memory. This class 
 * provides a way to read the integers one by one.
 * @param <T>
 */
public class StreamFixedListRangeIterator<T> implements IDisposableIterator<T> {
    /**
     * Reader set to the position at the start of the list.
     */
    private BinaryReader reader;
    /**
     * The number of items to read to form the array.
     */
    private final int count;
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
    private int currentIndex;
    
    private final int startIndex;    
    
    /**
     * An enumerable that can be used to read through the entries.
     * @param count of the number of items to return
     * @param entityFactory to create new items of the type required
     * @param dataSet the entities returned will relate to
     * @param startIndex of the item in the fixed list
     * @param startPosition of the first byte in the byte stream
     * @throws java.io.IOException 
     */
    public StreamFixedListRangeIterator(
            BaseEntityFactory<T> entityFactory, 
            Dataset dataSet,
            int startPosition,
            int startIndex,
            int count) throws IOException {
        this.dataSet = dataSet;
        this.entityFactory = entityFactory;
        this.currentIndex = startIndex;
        this.startIndex = startIndex;
        this.reader = dataSet.pool.getReader();
        this.reader.setPos(startPosition);
        this.count = count;
    }
    
    /**
     * Returns the reader to the pool.
     * @param disposing 
     */
    protected void disposing(boolean disposing) {
        if (reader != null) {
            dataSet.pool.release(reader);
            reader = null;
        }
    }
    
    /**
     * Return reader to the pool when the enumerable is
     * disposed of.
     */
    @Override
    public void dispose() {
        disposing(true);
    }

    /**
     * When garbage collected, make sure the reader is returned to the pool.
     * @throws Throwable 
     */
    @Override
    protected void finalize() throws Throwable {
        disposing(false);
    }

    @Override
    public boolean hasNext() {
        return currentIndex - startIndex < count;
    }

    @Override
    public T next() {
        T item = null;
        try {
            item = (T)entityFactory.create(dataSet, currentIndex, reader);
            currentIndex++;
        } catch (IOException ex) {
            Logger.getLogger(StreamFixedListRangeIterator.class.getName())
                                            .log(Level.SEVERE, null, ex);
        }
        return item;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException(
                "Items can not be removed from DataSet lists.");    }
}
