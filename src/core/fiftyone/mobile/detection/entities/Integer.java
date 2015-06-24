/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fiftyone.mobile.detection.entities;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.readers.BinaryReader;

/**
 * A integer item in a list of integers.
 */
public class Integer extends BaseEntity {
    /**
     * The index of the signature in the list of signatures.
     */
    public int value;
    
    /**
     * Constructs a new instance of Integer.
     * @param dataSet data set whose strings list the string is contained within.
     * @param offsetOrIndex The index in the data structure to the integer.
     * @param reader Binary reader positioned at the start of the Integer.
     */
    public Integer(Dataset dataSet, int offsetOrIndex, BinaryReader reader) {
        super(dataSet, offsetOrIndex);
        value = reader.readInt32();
    }
    
    /**
     * The index of the signature in the list of signatures.
     * @return index of the signature in the list of signatures.
     */
    public int getValue() {
        return value;
    }
}
