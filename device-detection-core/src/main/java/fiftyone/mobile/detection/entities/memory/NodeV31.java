/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
package fiftyone.mobile.detection.entities.memory;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.BaseEntity;
import fiftyone.mobile.detection.entities.NodeIndex;
import fiftyone.mobile.detection.factories.NodeFactoryShared;
import fiftyone.mobile.detection.readers.BinaryReader;

/**
 * All data is loaded into memory when the entity is constructed.
 */
public class NodeV31 extends Node{
    /**
     * An array of the ranked signature indexes for the node.
     */
    private final int[] rankedSignatureIndexes;
    
    /**
     * Constructs a new instance of NodeV31.
     * @param dataSet The data set the node is contained within.
     * @param offset The offset in the data structure to the node.
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     */
    public NodeV31(Dataset dataSet, int offset, BinaryReader reader) {
        super(dataSet, offset, reader);
        rankedSignatureIndexes = BaseEntity.readIntegerArray(reader, rankedSignatureCount);
    }
    
    /**
     * An array of the ranked signature indexes for the node.
     * @return An array of the ranked signature indexes for the node.
     */
    @Override
    public int[] getRankedSignatureIndexes() {
        return rankedSignatureIndexes;
    }
    
    /**
     * Reads the ranked signature count from a 4 byte integer.
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     * @return The count of ranked signatures associated with the node.
     */
    @Override
    public int readerRankedSignatureCount(BinaryReader reader) {
        return reader.readInt32();
    }
    
    /**
     * Used by the constructor to read the variable length list of child
     * node indexes associated with the node. Returns node indexes from V32
     * data format.
     * @param dataSet The data set the node is contained within.
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     * @param offset The offset in the data structure to the node.
     * @param count  The number of node indexes that need to be read.
     * @return An array of child node indexes for the node.
     */
    @Override
    public NodeIndex[] readNodeIndexes(Dataset dataSet, BinaryReader reader, 
            int offset, int count) {
        return NodeFactoryShared.readNodeIndexesV31(
                dataSet, reader, offset, count);
    }
}
