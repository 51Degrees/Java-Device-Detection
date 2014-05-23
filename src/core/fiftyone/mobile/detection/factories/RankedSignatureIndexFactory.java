package fiftyone.mobile.detection.factories;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.RankedSignatureIndex;
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
public class RankedSignatureIndexFactory extends BaseEntityFactory<RankedSignatureIndex> {

    /**
     * Creates a new instance of the ranked signature index from the source
     * stream.
     *
     * @param dataSet the ranked signature index will relate to.
     * @param index index of the ranked signature index.
     * @param reader connected to the source stream.
     * @return the ranked signature index pointing to the index of the
     * signature.
     */
    @Override
    public RankedSignatureIndex create(Dataset dataSet, int index,
            BinaryReader reader) {
        return new RankedSignatureIndex(dataSet, index, reader);
    }

    /**
     * @return the length of a ranked signature index integer.
     */
    @Override
    public int getLength() {
        return 4;
    }
}
