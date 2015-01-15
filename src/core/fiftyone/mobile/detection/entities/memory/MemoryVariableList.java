package fiftyone.mobile.detection.entities.memory;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.BaseEntity;
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
 * A readonly list of variable length entity types held in memory. <p> Entities
 * in the underlying data structure are either fixed length where the data that
 * represents them always contains the same number of bytes, or variable length
 * where the number of bytes to represent the entity varies. <p> This class uses
 * the offset of the first byte of the entities data in the underlying data
 * structure in the accessor. As such the list isn't being used as a traditional
 * list because items are not retrieved by their index in the list, but by there
 * offset in the underlying data structure. <p> The constructor will read the
 * header information about the underlying data structure and the entities are
 * added to the list when the Read method is called. <p> The class supports
 * source stream that do not support seeking. <p> Should not be referenced
 * directly.
 *
 * @param <T> The type of BaseEntity the list will contain
 */
public class MemoryVariableList<T extends BaseEntity> extends BaseList<T> {

    /**
     * Constructs a new instance of VariableList of type T
     *
     * @param dataSet The DetectorDataSet being created
     * @param reader Reader connected to the source data structure and
     * positioned to start reading
     * @param entityFactory Interface implementation used to create and size new
     * entities of type T
     */
    public MemoryVariableList(Dataset dataSet, BinaryReader reader,
            BaseEntityFactory<T> entityFactory) {
        super(dataSet, reader, entityFactory);
    }

    /**
     * Reads the list into memory.
     *
     * @param reader Reader connected to the source data structure and
     * positioned to start reading
     * @throws IOException indicates an I/O exception occurred
     */
    public void read(BinaryReader reader) throws IOException {
        for (int index = 0, offset = 0; index < header.getCount(); index++) {
            T entity = entityFactory.create(dataSet, offset, reader);
            array.add(index, entity);
            offset += entityFactory.getLength(entity);
        }
    }

    /**
     * Accessor for the variable list. <p> As all the entities are held in
     * memory and in ascending order of offset a BinarySearch can be used to
     * determine the one that relates to the given offset rapidly.
     *
     * @param offset The offset position in the data structure to the entity to
     * be returned from the list
     * @return Entity at the offset requested
     */
    public T get(int offset) {
        return array.get(binarySearch(offset));
    }

    /**
     * Uses a divide and conquer method to search the ordered list of entities
     * that are held in memory.
     *
     * @param offset The offset position in the data structure to the entity to
     * be returned from the list
     * @return Entity at the offset requested
     */
    private int binarySearch(int offset) {
        int lower = 0;
        int upper = size() - 1;

        while (lower <= upper) {
            int middle = lower + (upper - lower) / 2;
            int comparisonResult = array.get(middle).getIndex() - offset;
            if (comparisonResult == 0) {
                return middle;
            } else if (comparisonResult > 0) {
                upper = middle - 1;
            } else {
                lower = middle + 1;
            }
        }

        return -1;
    }
}
