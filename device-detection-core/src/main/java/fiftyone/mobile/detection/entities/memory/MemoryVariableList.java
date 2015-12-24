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
package fiftyone.mobile.detection.entities.memory;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.BaseEntity;
import fiftyone.mobile.detection.factories.BaseEntityFactory;
import fiftyone.mobile.detection.readers.BinaryReader;
import fiftyone.mobile.detection.search.SearchBase;
import java.io.IOException;
import java.util.List;

/**
 * A readonly list of variable length entity types held in memory. 
 * <p>
 * Entities in the underlying data structure are either fixed length where the 
 * data that represents them always contains the same number of bytes, or 
 * variable length where the number of bytes to represent the entity varies.
 * <p>
 * This class uses the offset of the first byte of the entities data in the 
 * underlying data structure in the accessor. As such the list isn't being 
 * used as a traditional list because items are not retrieved by their index 
 * in the list, but by there offset in the underlying data structure.
 * <p>
 * The constructor will read the header information about the underlying data 
 * structure and the entities are added to the list when the Read method 
 * is called.
 * <p>
 * The class supports source stream that do not support seeking.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 *
 * @param <T> The type of BaseEntity the list will contain
 */
public class MemoryVariableList<T extends BaseEntity> extends 
        MemoryBaseList<T> {
    
    private final SearchVariableList search = new SearchVariableList();
    
    /**
     * Constructs a new instance of VariableList of type T.
     *
     * @param dataSet The DetectorDataSet being created.
     * @param reader BinaryReader connected to the source data structure and
     *               positioned to start reading.
     * @param entityFactory Interface implementation used to create and size new
     *                      entities of type T.
     */
    public MemoryVariableList(Dataset dataSet, BinaryReader reader,
            BaseEntityFactory<T> entityFactory) {
        super(dataSet, reader, entityFactory);
    }

    /**
     * Reads the list into memory.
     * 
     * @param reader BinaryReader connected to the source data structure and
     *               positioned to start reading.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @Override
    public void read(BinaryReader reader) throws IOException {
        int offset = 0;
        for (int index = 0; index < header.getCount(); index++) {
            T entity = (T)entityFactory.create(dataSet, offset, reader);
            super.array.add(entity);
            offset += entityFactory.getLength(entity);
        }
    }

    /**
     * Accessor for the variable list.
     * <p>
     * As all the entities are held in memory and in ascending order of 
     * offset a BinarySearch can be used to determine the one that relates to 
     * the given offset rapidly.
     *
     * @param offset the offset position in the data structure to the entity to
     *               be returned from the list.
     * @return Entity at the offset requested.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @Override
    public T get(int offset) throws IOException {
        int index = search.binarySearch(offset);
        if (index >= 0)
            return array.get(index);
        return null;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Private static class for binary search access.">
    /**
     * Used to search the list using the offset key for the items.
     */
    private class SearchVariableList extends SearchBase<T, Integer, List<T>> {

        @Override
        protected int getCount(List<T> list) {
            return list.size();
        }

        @Override
        protected T getValue(List<T> list, int index) throws IOException {
            return list.get(index);
        }        
        
        @Override
        protected int compareTo(T item, Integer key) throws IOException {
            return item.compareTo(key);
        }
        
        int binarySearch(Integer offset) throws IOException {
            return super.binarySearch(array, offset);
        }
    }
    // </editor-fold>
}
