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
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all entities in the DetectorDataSet. Contains common 
 * properties and methods for all data types contained in the data set.
 * <p> 
 * All entities must belong to a data set and contain a unique integer key. 
 * Class provides this functionality along with many common methods used 
 * by multiple entities.
 * <p>
 * All entities in this package extend this class. Every entity contains a 
 * unique integer key.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 * <p>
 * For more information see: 
 * <a href="https://51degrees.com/support/documentation/device-detection-data-model">
 * 51Degrees pattern data model</a>.
 */
public class BaseEntity {

    /**
     * List if powers used to determine numeric differences.
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
     * 
     * @param value Byte value to be checked.
     * @return True if the value is an ASCII numeric character.
     */
    public static boolean getIsNumeric(byte value) {
        return (value >= (byte)'0' && value <= (byte)'9');
    }
    
    /**
     * Constructs the base item for the data set and index provided.
     *
     * @param dataSet The data set the item is contained within.
     * @param offsetOrIndex The index of the item within the dataset.
     */
    BaseEntity(Dataset dataSet, int offsetOrIndex) {
        this.dataSet = dataSet;
        this.index = offsetOrIndex;
    }

    /**
     * The unique index of the item in the collection of items, or the unique 
     * offset to the item in the source data structure.
     * 
     * @param offsetOrIndex The index of the item within the dataset.
     * @return integer difference between two offsets.
     */
    public int compareTo(int offsetOrIndex) {
        return index - offsetOrIndex;
    }
    
    /**
     * Compares entities based on their Index properties.
     * 
     * @param other The entity to be compared against.
     * @return The position of one entity over the other.
     */
    public int compareTo(BaseEntity other) {
        return compareTo(other.index);
    }
    
    /**
     * Reads an integer array where the first integer is the number of following
     * integers.
     *
     * @param reader Reader set to the position at the start of the list
     * @param count The number of integers to read to form the array
     * @return An array of integers.
     */
    protected static int[] readIntegerArray(BinaryReader reader, int count) {
        int[] array = new int[count];
        for (int i = 0; i < count; i++) {
            array[i] = reader.readInt32();
        }
        return array;
    }
    
    /**
     * Reads an integer list where the first integer is the number of following
     * integers.
     *
     * @param reader Reader set to the position at the start of the list
     * @param count The number of integers to read to form the array
     * @return A List of integers.
     */
    protected static List<Integer> readIntegerList(BinaryReader reader, 
                                                   int count) {
        List<Integer> array = new ArrayList<Integer>(count);
        for (int i = 0; i < count; i++) {
            array.add(reader.readInt32());
        }
        return array;
    }
    
    /**
     * Returns an integer representation of the characters between start and 
     * end. Assumes that all the characters are numeric characters.
     * 
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

    /**
     * @return DataSet used to create this base entity.
     */
    protected Dataset getDataSet() {
        return dataSet;
    }

    /**
     * @return Index of this base entity.
     */
    public int getIndex() {
        return index;
    }
}
