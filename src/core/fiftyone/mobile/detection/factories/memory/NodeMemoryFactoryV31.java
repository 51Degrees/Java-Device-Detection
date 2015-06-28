package fiftyone.mobile.detection.factories.memory;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.Node;
import fiftyone.mobile.detection.factories.NodeFactory;
import fiftyone.mobile.detection.factories.NodeFactoryShared;
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
 * Factory class used to create the new instances of Node V3.1. object.
 */
public class NodeMemoryFactoryV31 extends NodeFactory {

    /**
     * Implements the creation of a new instance of Node. Needs to be 
     * implemented in a subclass.
     * @param dataSet The data set whose node list the node is contained within.
     * @param index The offset to the start of the node within the string data 
     * structure.
     * @param reader Binary reader positioned at the start of the Node.
     * @return A new instance of a Node.
     */
    @Override
    protected fiftyone.mobile.detection.entities.Node construct(
            Dataset dataSet, int index, BinaryReader reader) {
        return new fiftyone.mobile.detection.entities.memory.NodeV31(
                dataSet, index, reader);
    }
    
    /**
     * Returns the length of the NodeV31 entity provided.
     * @param entity An entity of type Node who length is required.
     * @return The number of bytes used to store the node.
     */
    @Override
    public int getLength(fiftyone.mobile.detection.entities.Node entity) {
        return getBaseLength() +
                // Length of the ranked signatures count number
                DetectionConstants.SIZE_OF_INT + 
                (entity.getChildrenLength() * 
                        NodeFactoryShared.getNodeIndexLengthV31()) + 
                (entity.getNumericChildrenLength() * 
                        getNodeNumericIndexLength()) +
                entity.signatureIndexes.length;
                
    }
    
}
