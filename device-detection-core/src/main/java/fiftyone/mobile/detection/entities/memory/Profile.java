/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fiftyone.mobile.detection.entities.memory;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.BaseEntity;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;

/**
 * All data is loaded into memory when the entity is constructed.
 */
public class Profile extends fiftyone.mobile.detection.entities.Profile {
    /**
     * Array of value indexes associated with the profile.
     */
    private int[] valueIndexes;
    /**
     * Array of signature indexes associated with the profile.
     */
    private int[] signatureIndexes;
    
    /**
     * Constructs a new instance of the Profile.
     * @param dataSet The data set whose profile list the profile will be 
     * contained within.
     * @param offset The offset position in the data structure to the profile.
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     */
    public Profile(Dataset dataSet, int offset, BinaryReader reader) {
        super(dataSet, offset, reader);
        int valueIndexesCount = reader.readInt32();
        int signatureIndexesCount = reader.readInt32();
        this.valueIndexes = BaseEntity.readIntegerArray(reader, valueIndexesCount);
        this.signatureIndexes = BaseEntity.readIntegerArray(reader, signatureIndexesCount);
    }
    
    /**
     * Get array of value indexes associated with the profile.
     * @return Array of value indexes associated with the profile.
     */
    @Override
    public int[] getValueIndexes() {
        return valueIndexes;
    }
    
    /**
     * Get array of signature indexes associated with the profile.
     * @return Array of signature indexes associated with the profile.
     */
    @Override
    public int[] getSignatureIndexes() {
        return signatureIndexes;
    }
    
    /**
     * Initialises the data and releases any memory for collection.
     * @throws java.io.IOException
     */
    @Override
    public void init() throws IOException {
        super.init();
        valueIndexes = null;
        signatureIndexes = null;
    }
}
