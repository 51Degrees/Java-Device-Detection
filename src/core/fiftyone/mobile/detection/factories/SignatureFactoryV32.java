package fiftyone.mobile.detection.factories;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.Signature;
import fiftyone.mobile.detection.entities.SignatureV32;
import fiftyone.mobile.detection.readers.BinaryReader;
import fiftyone.properties.DetectionConstants;

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
 * Class handles the creation of the old v 3.2 signature entities.
 */
public class SignatureFactoryV32 extends BaseEntityFactory<Signature> {
    /**
     * The length of each signature record in the dataset.
     * Equivalent to sizeof(byte) + sizeof(int) + sizeof(int) in C#.
     * byte = count of nodes associated with the signature
     * int = first index of the node offset in signaturesnodes
     * int = rank of the signature
     */
    private static final int NODES_LENGTH = 
            DetectionConstants.SIZE_OF_BYTE +
            DetectionConstants.SIZE_OF_INT +
            DetectionConstants.SIZE_OF_INT;
    
    /**
     * Length of the signature in bytes.
     */
    private final int recordLength;
    
    /**
     * Constructs a new instance of SignatureFactoryV32.
     * @param dataSet The data set the factory will create signatures for.
     */
    public SignatureFactoryV32(Dataset dataSet) {
        this.recordLength = 
            (dataSet.signatureProfilesCount * DetectionConstants.SIZE_OF_INT) +
            NODES_LENGTH;
    }
    
    /**
     * Creates a new instance of SignatureV32.
     * @param dataSet The data set whose signature list the value is contained 
     * within.
     * @param index The index of the signature within the values data structure.
     * @param reader Binary reader positioned at the start of the signature.
     * @return A new instance of a Signature.
     */
    @Override
    public Signature create(Dataset dataSet, int index, BinaryReader reader) {
        return new SignatureV32(dataSet, index, reader);
    }
    
    /**
     * The length of the signature.
     * @return Length of the signature in bytes.
     */
    @Override
    public int getLength() {
        return recordLength;
    }
}