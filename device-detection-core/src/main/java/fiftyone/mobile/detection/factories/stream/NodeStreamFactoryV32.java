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
package fiftyone.mobile.detection.factories.stream;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.Node;
import fiftyone.mobile.detection.factories.NodeFactory;
import fiftyone.mobile.detection.readers.BinaryReader;

/**
 * Factory used to create stream Entities.Node entities of version 3.2.
 */
public class NodeStreamFactoryV32 extends NodeFactory {

    /**
     * Constructs a new instance of NodeStreamFactoryV32.
     */
    public NodeStreamFactoryV32() {}

    /**
     * Constructs a new "Entities.Stream.NodeV32 entity from 
     * the offset provided.
     * @param dataSet The data set the node is contained within.
     * @param index The offset in the data structure to the node.
     * @param reader  Reader connected to the source data structure and 
     * positioned to start reading.
     * @return A new Entities.Node entity from the data set.
     */
    @Override
    protected Node construct(Dataset dataSet, int index, BinaryReader reader) {
        return new fiftyone.mobile.detection.entities.stream.NodeV32(
                (fiftyone.mobile.detection.entities.stream.Dataset)dataSet, 
                index, 
                reader);
    }
    
}
