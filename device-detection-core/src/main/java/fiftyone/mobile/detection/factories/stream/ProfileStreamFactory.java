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
package fiftyone.mobile.detection.factories.stream;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.Profile;
import fiftyone.mobile.detection.entities.stream.Pool;
import fiftyone.mobile.detection.readers.BinaryReader;

/**
 *
 * @author mike
 */
public class ProfileStreamFactory extends fiftyone.mobile.detection.factories.ProfileFactory {
    
    private final Pool pool;
    
    public ProfileStreamFactory(Pool pool) {
        this.pool = pool;
    }

    @Override
    protected Profile construct(Dataset dataSet, int index, BinaryReader reader) {
        return new fiftyone.mobile.detection.entities.stream.Profile((fiftyone.mobile.detection.entities.stream.Dataset)dataSet, index, reader);
    }
}
