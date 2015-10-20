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
package fiftyone.mobile.detection.entities;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.readers.BinaryReader;

/**
 * Base class for all entities in the DetectorDataSet. <p> All entities must
 * belong to a data set and contain a unique integer key. This class provides
 * this functionality along with many common methods used by multiple entities.
 * <p>
 *
 * For more information see http://51degrees.com/Support/Documentation/Java
 */
/**
 * Common properties and methods for all data types contained in the data set.
 */
public class BaseEntity {

    /**
     * TODO: get description.
     */
    private final static int[] POWERS = new int[] { 1, 10, 100, 1000, 10000 };
    
    /**
     * The data set the item relates to.
     */
    public final Dataset dataSet;
    /**
     * The unique index of the item in the collection of items, or the unique
     * offset to the item in the source data structure.
     */
    public final int index;

    /**
     * Determines if the value is an ASCII numeric value.
     * @param value Byte value to be checked.
     * @return True if the value is an ASCII numeric character.
     */
    public static boolean getIsNumeric(byte value) {
        return (value >= (byte)'0' && value <= (byte)'9');
    }
    
    /**
     * Constructs the base item for the data set and index provided.
     *
     * @param dataSet The data set the item is contained within
     * @param index The index of the item within the dataset
     */
    BaseEntity(Dataset dataSet, int offsetOrIndex) {
        this.dataSet = dataSet;
        this.index = offsetOrIndex;
    }

    /**
     * Compares entities based on their Index properties.
     * @param other The entity to be compared against.
     * @return The position of one entity over the other.
     */
    public int compareTo(BaseEntity other) {
        // Following is equivalnt to Index.CompareTo(other.Index) in c#.
        if (this.index > other.index)
            return 1;
        else if (this.index == other.index)
            return 0;
        else
            return -1;
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
            int comparisonResult = list[middle].index
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
    protected static int[] readIntegerArray(BinaryReader reader, int count) {
        int[] array = new int[count];
        for (int i = 0; i < count; i++) {
            array[i] = reader.readInt32();
        }
        return array;
    }
    
    /**
     * Returns an integer representation of the characters between start and 
     * end. Assumes that all the characters are numeric characters.
     * @param array Array of characters with numeric characters present 
     * between start and end.
     * @param start The first character to use to convert to a number.
     * @param length The number of characters to use in the conversion.
     * @return integer representation of the characters between start and end.
     */
    public static int getNumber(byte[] array, int start, int length) {
        int value = 0;
        for (int i = (start + length - 1), p = 0; i >= start 
                      && p < POWERS.length; i--, p++) {
            value += POWERS[p] * ((byte)array[i] - (byte)'0');
        }
        return value;
    }

    protected Dataset getDataSet() {
        return dataSet;
    }

    public int getIndex() {
        return index;
    }
}
