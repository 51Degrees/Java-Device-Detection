package fiftyone.mobile.detection.entities;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.readers.BinaryReader;

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
 * Base class for all entities in the DetectorDataSet. <p> All entities must
 * belong to a data set and contain a unique integer key. This class provides
 * this functionality along with many common methods used by multiple entities.
 * <p>
 *
 * For more information see http://51degrees.mobi/Support/Documentation/Java
 */
/**
 * Common properties and methods for all data types contained in the data set.
 */
public class BaseEntity {

    /**
     * The data set the item relates to.
     */
    private final Dataset dataSet;
    /**
     * The unique index of the item in the collection of items, or the unique
     * offset to the item in the source data structure.
     */
    private final int offsetOrIndex;

    /**
     * Constructs the base item for the data set and index provided.
     *
     * @param dataSet The data set the item is contained within
     * @param index The index of the item within the dataset
     */
    BaseEntity(Dataset dataSet, int offsetOrIndex) {
        this.dataSet = dataSet;
        this.offsetOrIndex = offsetOrIndex;
    }

    /**
     * Uses a divide and conquer method to search the ordered list of indexes.
     *
     * @param list List of entities to be searched
     * @param indexOrOffset The index or offset to be sought
     * @return The index of the entity in the list or twos complement of insert
     * index
     */
    protected int binarySearch(BaseEntity[] list, int indexOrOffset) {
        int lower = 0;
        int upper = list.length - 1;

        while (lower <= upper) {
            int middle = lower + (upper - lower) / 2;
            int comparisonResult = list[middle].offsetOrIndex
                    - indexOrOffset;
            if (comparisonResult == 0) {
                return middle;
            } else if (comparisonResult > 0) {
                upper = middle - 1;
            } else {
                lower = middle + 1;
            }
        }

        return ~lower;
    }

    /**
     * Reads an integer array where the first integer is the number of following
     * integers.
     *
     * @param reader Reader set to the position at the start of the list
     * @param count The number of integers to read to form the array
     * @return An array of integers
     */
    static int[] readIntegerArray(BinaryReader reader, int count) {
        int[] array = new int[count];
        for (int i = 0; i < count; i++) {
            array[i] = reader.readInt32();
        }
        return array;
    }

    protected Dataset getDataSet() {
        return dataSet;
    }

    public int getIndex() {
        return offsetOrIndex;
    }
}
