package fiftyone.mobile.detection.entities;

import fiftyone.mobile.detection.Dataset;
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
 * Maps a ranked signature index to the signature index.
 */
public class RankedSignatureIndex extends BaseEntity {

    /**
     * @return The index of the signature in the list of signatures.
     */
    public int getSignatureIndex() {
        return signatureIndex;
    }
    private int signatureIndex;

    public RankedSignatureIndex(Dataset dataSet, int index,
            BinaryReader reader) {
        super(dataSet, index);
        signatureIndex = reader.readInt32();
    }
    
    /**
     * Sets the associated signatures rank property.
     * @throws IOException indicates an I/O exception occurred
     */
    public void init() throws IOException {
        getDataSet().signatures.get(signatureIndex).rank = getIndex();
    }
}
