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
import fiftyone.mobile.detection.entities.Map;
import fiftyone.mobile.detection.readers.BinaryReader;

/**
 * Creates new instances of a Map entity.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 */
public class MapFactory extends BaseEntityFactory<Map> {

    /**
     * Creates a new instance of Map.
     * 
     * @param dataSet data set whose data structure includes map values.
     * @param index to the start of the Map within the data structure.
     * @param reader Binary reader positioned at the start of the Map.
     * @return A new instance of a Map.
     */
    @Override
    public Map create(Dataset dataSet, int index,
            BinaryReader reader) {
        return new Map(dataSet, index, reader);
    }
}
