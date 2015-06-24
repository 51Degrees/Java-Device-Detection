/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fiftyone.mobile.detection.factories;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.readers.BinaryReader;
import fiftyone.mobile.detection.entities.Integer;
import java.io.IOException;

/**
 * Creates a new instance of Integer.
 */
public class IntegerFactory extends BaseEntityFactory<Integer> {

    /**
     * Creates a new instance of Integer.
     * @param dataSet data set whose data structure includes integer values.
     * @param index index to the start of the Integer within the data structure.
     * @param reader Binary reader positioned at the start of the Integer.
     * @return A new instance of an Integer.
     * @throws IOException 
     */
    @Override
    public Integer create(Dataset dataSet, int index, BinaryReader reader) throws IOException {
        return new Integer(dataSet, index, reader);
    }
    
    /**
     * Returns the length of the Integer entity
     * @return Length in bytes of the RankedSignatureIndex.
     */
    @Override
    public int getLength() {
        return (java.lang.Integer.SIZE / java.lang.Byte.SIZE);
    }
}
