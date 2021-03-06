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
import fiftyone.mobile.detection.entities.Component;
import fiftyone.mobile.detection.entities.ComponentV32;
import fiftyone.mobile.detection.readers.BinaryReader;

/**
 * Creates new instances of a Component entity for data file of version 3.2.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 */
public class ComponentFactoryV32 extends ComponentFactory {
    
    /**
     * Creates a new instance of ComponentV32.
     * 
     * @param dataSet The data set whose components list the component is 
     * contained within.
     * @param index Index of the entity within the list.
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     * @return A new instance of ComponentV32.
     */
    @Override
    public Component create(Dataset dataSet, int index, BinaryReader reader) {
        return new ComponentV32(dataSet, index, reader);
    }
}
