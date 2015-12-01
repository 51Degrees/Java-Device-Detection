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

import fiftyone.mobile.detection.entities.BaseEntity;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;

/**
 * Profile entity with stream specific data access implementation.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 */
public class Profile extends fiftyone.mobile.detection.entities.Profile {
    private final int position;
    private final int valueIndexesCount;
    private final int signatureIndexesCount;
    private final Pool pool;
    
    /**
     * Constructs a new Profile object.
     * 
     * @param dataSet the data set the profile is contained within.
     * @param index the index in the data structure to the profile.
     * @param reader BinaryReader object to be used.
     */
    public Profile(Dataset dataSet, int index, BinaryReader reader) {
        super(dataSet, index, reader);
        this.pool = dataSet.pool;
        valueIndexesCount = reader.readInt32();
        signatureIndexesCount = reader.readInt32();
        position = reader.getPos();
    }
    
    /** 
     * @return Array of value indexes associated with the profile.
     * @throws IOException if there was a problem reading from the data file.
     */
    @Override
    @SuppressWarnings("DoubleCheckedLocking")
    public int[] getValueIndexes() throws IOException {
        int[] localValueIndexes = valueIndexes;
        if(localValueIndexes == null) {
            synchronized(this) {
                localValueIndexes = valueIndexes;
                if(localValueIndexes == null) {
                    BinaryReader reader = pool.getReader();
                    reader.setPos(position);
                    valueIndexes = localValueIndexes = 
                            BaseEntity.readIntegerArray(reader, valueIndexesCount);
                    pool.release(reader);
                }
            }
        }
        return localValueIndexes;
    }
    
    /**
     * @return Array of signature indexes associated with the profile.
     * @throws IOException if there was a problem reading from the data file.
     */
    @Override
    @SuppressWarnings("DoubleCheckedLocking")
    public int[] getSignatureIndexes() throws IOException {
        int[] localSignatureIndexes = signatureIndexes;
        if(localSignatureIndexes == null) {
            synchronized(this) {
                localSignatureIndexes = signatureIndexes;
                if (localSignatureIndexes == null) {
                    BinaryReader reader = pool.getReader();
                    int offset = valueIndexesCount * (Integer.SIZE / Byte.SIZE);
                    reader.setPos(position + offset);
                    signatureIndexes = localSignatureIndexes = 
                            BaseEntity.readIntegerArray(reader, signatureIndexesCount);
                    pool.release(reader);
                }
            }
        }
        return localSignatureIndexes;
    }
}
