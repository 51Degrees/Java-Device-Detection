/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
 * Caversham, Reading, Berkshire, United Kingdom RG4 7BY
 * 
 * This Source Code Form is the subject of the following patents and patent
 * applications, owned by 51Degrees Mobile Experts Limited of 5 Charlotte
 * Close, Caversham, Reading, Berkshire, United Kingdom RG4 7BY: 
 * European Patent No. 2871816;
 * European Patent Application No. 17184134.9;
 * United States Patent Nos. 9,332,086 and 9,350,823; and
 * United States Patent Application No. 15/686,066.
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
package fiftyone.mobile.detection.factories;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.Signature;
import fiftyone.mobile.detection.entities.SignatureV31;
import fiftyone.mobile.detection.readers.BinaryReader;
import fiftyone.properties.DetectionConstants;
import java.io.IOException;

/**
 * Class handles the creation of the old v 3.1 signature entities.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 */
public class SignatureFactoryV31 extends BaseEntityFactory<Signature>{
    
    /**
     * Length of the signature in bytes.
     */
    private final int recordLength;
    
    /**
     * Constructs a new instance of SignatureFactoryV31.
     * 
     * @param dataSet The data set the factory will create signatures for.
     */
    public SignatureFactoryV31(Dataset dataSet) {
        recordLength = 
            (dataSet.signatureProfilesCount * DetectionConstants.SIZE_OF_INT) +
             (dataSet.signatureNodesCount * DetectionConstants.SIZE_OF_INT);
    }
    
    /**
     * Creates a new instance of SignatureV31.
     * 
     * @param dataSet The data set whose signature list the value is contained 
     * within.
     * @param index The index of the signature within the values data structure.
     * @param reader  Binary reader positioned at the start of the signature.
     * @return A new instance of a Signature.
     * @throws IOException if there was a problem reading data file.
     */
    @Override
    public Signature create(Dataset dataSet, int index, BinaryReader reader) 
            throws IOException {
        return new SignatureV31(dataSet, index, reader);
    }
    
    /**
     * The length of the signature.
     * 
     * @return Length of the signature in bytes.
     */
    @Override
    public int getLength() {
        return recordLength;
    }
}
