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
package fiftyone.mobile.detection.entities.stream;

import fiftyone.mobile.detection.IClosableIterator;
import fiftyone.mobile.detection.WrappedIOException;
import fiftyone.mobile.detection.factories.BaseEntityFactory;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;

/**
 * Class implements the logic of IEnumerable in C#. When you need to read a 
 * specific number of integer objects from the data file it's best to do so 
 * on the as-needed basis instead of loading everything in to memory. This class 
 * provides a way to read the integers one by one.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 * 
 * @param <T> The type of BaseEntity to iterate.
 */
public class StreamFixedListRangeIterator<T> implements IClosableIterator<T> {
    /**
     * Reader set to the position at the start of the list.
     */
    private BinaryReader reader;
    /**
     * The number of items to read to form the array.
     */
    private final int count;
    /**
     * Reference to DataSet object used to obtain and release binary readers.
     */
    private final Dataset dataSet;
    /**
     * Used to create an entity of the corresponding type.
     */
    private final BaseEntityFactory<T> entityFactory;
    /**
     * Current position.
     */
    private int currentIndex;
    /**
     * Start position.
     */
    private final int startIndex;    
    
    /**
     * An enumerable that can be used to read through the entries.
     * 
     * @param count of the number of items to return
     * @param entityFactory to create new items of the type required
     * @param dataSet the entities returned will relate to
     * @param startIndex of the item in the fixed list
     * @param startPosition of the first byte in the byte stream
     * @throws java.io.IOException if there was a problem accessing data file.
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
     */
    @Override
    public void close() {
        if (reader != null) {
            dataSet.pool.release(reader);
            reader = null;
        }
    }

    /**
     * @return True if there are more elements to read, False otherwise.
     */
    @Override
    public boolean hasNext() {
        return currentIndex - startIndex < count;
    }

    /**
     * @return next element in the list.
     */
    @Override
    public T next() {
        T item = null;
        try {
            item = (T)entityFactory.create(dataSet, currentIndex, reader);
            currentIndex++;
        } catch (IOException ex) {
            throw new WrappedIOException(ex.getMessage());
        }
        return item;
    }

    /**
     * Unsupported, do not use.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove is not a valid action "
                + "for the StreamFixedListRangeIterator.");    
    }
}
