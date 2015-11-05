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
package fiftyone.mobile.detection.entities.memory;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.IntegerEntity;
import fiftyone.mobile.detection.entities.NodeIndex;
import fiftyone.mobile.detection.factories.NodeFactoryShared;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;

/**
 * All data is loaded into memory when the entity is constructed.
 */
public class NodeV32 extends Node{

    private int nodeRankedSignatureValue;
    
    /**
     * Constructs a new instance of NodeV32.
     * @param dataSet The data set the node is contained within.
     * @param offset The offset in the data structure to the node.
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     */
    public NodeV32(Dataset dataSet, int offset, BinaryReader reader) {
        super(dataSet, offset, reader);
        if (rankedSignatureCount > 0) {
            nodeRankedSignatureValue = reader.readInt32();
        }
    }
    
    /**
     * Loads all the ranked signature indexes for the node.
     * @throws java.io.IOException if there was a problem reading from the data 
     * file.
     */
    @Override
    public void init() throws IOException {
        super.init();
        if (rankedSignatureIndexes == null) {
            rankedSignatureIndexes = getRankedSignatureIndexesAsArray();
        }
    }
    
    /**
     * Returns ranked signature indexes as array.
     * @return ranked signature indexes as array.
     * @throws IOException 
     */
    private int[] getRankedSignatureIndexesAsArray() throws IOException {
        int[] rsi = new int[rankedSignatureCount];
        if (rankedSignatureCount == 1) {
            // The value of _nodeRankedSignatureIndex is the ranked signature
            // index when the node only relates to 1 signature.
            rsi[0] = nodeRankedSignatureValue;
        } else if (rankedSignatureCount > 1) {
            // Where the node relates to multiple signatures the 
            // _nodeRankedSignatureIndex relates to the first ranked signature 
            // index in DataSet.NodeRankedSignatureIndexes.
            for (int i = 0; i < rankedSignatureCount; i++) {
                IntegerEntity ie = (IntegerEntity)dataSet.
                        nodeRankedSignatureIndexes.
                        get(nodeRankedSignatureValue + i);
                rsi[i] = ie.getValue();
            }
        }
        return rsi;
    }

    /**
     * Reads the ranked signature count from a 2 byte ushort.
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     * @return The count of ranked signatures associated with the node.
     */
    @Override
    public int readerRankedSignatureCount(BinaryReader reader) {
        return reader.readUInt16();
    }

    /**
     * Used by the constructor to read the variable length list of child node 
     * indexes associated with the node. Returns node indexes from V32 data 
     * format.
     * @param dataSet The data set the node is contained within.
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     * @param offset The offset in the data structure to the node.
     * @param count The number of node indexes that need to be read.
     * @return An array of child node indexes for the node.
     */
    @Override
    protected NodeIndex[] readNodeIndexes(Dataset dataSet, BinaryReader reader, int offset, int count) {
        return NodeFactoryShared.readNodeIndexesV32(dataSet, reader, offset, count);
    }

    /**
     * Returns the ranked signature indexes. Init method is called if the array 
     * has not been initialised.
     * @return the ranked signature indexes.
     * @throws java.io.IOException
     */
    @Override
    @SuppressWarnings("DoubleCheckedLocking")
    public int[] getRankedSignatureIndexes() throws IOException {
        int[] localRankedSignatureIndexes = rankedSignatureIndexes;
        if (localRankedSignatureIndexes == null) {
            synchronized(this) {
                localRankedSignatureIndexes = rankedSignatureIndexes;
                if (localRankedSignatureIndexes == null) {
                    rankedSignatureIndexes = localRankedSignatureIndexes = getRankedSignatureIndexesAsArray();
                }
            }
        }
        return localRankedSignatureIndexes;
    }
    @SuppressWarnings("VolatileArrayField")
    private volatile int[] rankedSignatureIndexes;
}
