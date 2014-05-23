package fiftyone.mobile.detection.factories;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.Signature;
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
public class SignatureFactory extends BaseEntityFactory<Signature> {

    private int recordLength;

    public SignatureFactory(Dataset dataset) {
        recordLength =
                (dataset.getProfilesCount() * 4)
                + (dataset.getNodesCount() * 4);
    }

    @Override
    public Signature create(Dataset dataSet, int index,
            BinaryReader reader) {
        return new Signature(dataSet, index, reader);
    }

    @Override
    public int getLength() {
        return recordLength;
    }
}
