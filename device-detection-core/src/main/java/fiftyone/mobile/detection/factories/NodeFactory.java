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
package fiftyone.mobile.detection.factories;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.Node;
import fiftyone.mobile.detection.readers.BinaryReader;
import fiftyone.properties.DetectionConstants;

/**
 * Factory class used to create the new instances of Node object.
 */
public abstract class NodeFactory extends BaseEntityFactory<Node> {

    /**
     * The length of a numeric node index.
     * Equivalent of sizeof(short) + sizeof(int) in C#.
     */
    private static final int NODE_NUMERIC_INDEX_LENGTH = 
            DetectionConstants.SIZE_OF_SHORT +
            DetectionConstants.SIZE_OF_INT;
    
    /**
     * The basic length of a node for all supported versions.
     * Equivalent of sizeof(short) + sizeof(short) + sizeof(int) + sizeof(int) +
     * sizeof(short) + sizeof(short) in C#
     */
    private static final int BASE_LENGTH = 
            DetectionConstants.SIZE_OF_SHORT + 
            DetectionConstants.SIZE_OF_SHORT +
            DetectionConstants.SIZE_OF_INT +
            DetectionConstants.SIZE_OF_INT +
            DetectionConstants.SIZE_OF_SHORT +
            DetectionConstants.SIZE_OF_SHORT;
    
    /**
     * Creates a new instance of Node.
     * @param dataSet The data set whose node list the node is contained within.
     * @param index The offset to the start of the node within the string data 
     * structure.
     * @param reader Binary reader positioned at the start of the Node.
     * @return A new instance of a Node.
     */
    @Override
    public Node create(Dataset dataSet, int index, BinaryReader reader) {
        return construct(dataSet, index, reader);
    }
    
    /**
     * Returns the basic length of a node for all supported versions.
     * @return the basic length of a node for all supported versions.
     */
    public int getBaseLength() {
        return BASE_LENGTH;
    }
    
    /**
     * Returns The length of a numeric node index.
     * @return The length of a numeric node index.
     */
    public int getNodeNumericIndexLength() {
        return NODE_NUMERIC_INDEX_LENGTH;
    }
    
    /**
     * Implements the creation of a new instance of Node. Needs to be 
     * implemented in a subclass.
     * @param dataSet The data set whose node list the node is contained within.
     * @param index The offset to the start of the node within the string data 
     * structure.
     * @param reader Binary reader positioned at the start of the Node.
     * @return A new instance of a Node.
     */
    protected abstract Node construct(Dataset dataSet, int index, BinaryReader reader);
}
