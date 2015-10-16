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
package fiftyone.mobile.detection.entities.stream;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.IClosableIterator;
import fiftyone.mobile.detection.entities.IntegerEntity;
import fiftyone.mobile.detection.entities.NodeIndex;
import fiftyone.mobile.detection.factories.NodeFactoryShared;
import fiftyone.mobile.detection.readers.BinaryReader;
import fiftyone.properties.DetectionConstants;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a Node which can be used with the Stream data set. NumericChidren 
 * and RankedSignatureIndexes are not loaded into memory when the entity is 
 * constructed, they're only loaded from the data source when requested.
 */
public class NodeV32 extends Node {

    /**
     * A list of all the signature indexes that relate to this node.
     */
    private volatile int[] rankedSignatureIndexes;
    
    /**
     * Constructs a new instance of NodeV32.
     * @param dataSet The data set the node is contained within.
     * @param offset The offset in the data structure to the node.
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     */
    public NodeV32(fiftyone.mobile.detection.entities.stream.Dataset dataSet, 
                    int offset, BinaryReader reader) {
        super(dataSet, offset, reader);
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
    protected NodeIndex[] readNodeIndexes(Dataset dataSet, BinaryReader reader, 
            int offset, int count) {
        return NodeFactoryShared.readNodeIndexesV32(dataSet, reader, 
                                                    offset, count);
    }

    /**
     * Returns a list of all the signature indexes that relate to this node.
     * @return a list of all the signature indexes that relate to this node.
     */
    @Override
    public int[] getRankedSignatureIndexes() {
        int[] localRankedSignatureIndexes = rankedSignatureIndexes;
        if (localRankedSignatureIndexes == null) {
            synchronized (this) {
                localRankedSignatureIndexes = rankedSignatureIndexes;
                if (localRankedSignatureIndexes == null) {
                    try {
                        rankedSignatureIndexes = localRankedSignatureIndexes =
                                getRankedSignatureIndexesAsArray();
                    } catch (IOException ex) {
                        //TODO: handle exception.
                    }
                }
            }
        }
        return localRankedSignatureIndexes;
    }
    
    /**
     * Gets the ranked signature indexes array for the node.
     * @return An array of length _rankedSignatureCount filled with ranked 
     * signature indexes.
     */
    private int[] getRankedSignatureIndexesAsArray() throws IOException {
        int[] rsi = null;
        if (rankedSignatureCount == 0) {
            rsi = new int[0];
        } else {
            BinaryReader reader = null;
            try {
                reader = pool.getReader();
                
                // Position the reader after the numeric children.
                reader.setPos(numericChildrenPosition + ((
                        DetectionConstants.SIZE_OF_SHORT + 
                        DetectionConstants.SIZE_OF_INT) * 
                        getNumericChildrenLength()));
                
                // Read the index.
                int index = reader.readInt32();
                
                if (rankedSignatureCount == 1) {
                    // If the count is one then the value is the 
                    // ranked signature index.
                    rsi = new int[rankedSignatureCount];
                    rsi[0] = index;
                } else {
                    // If the count is greater than one then the value is the 
                    // index of the first ranked signature index in the merged 
                    // list.
                    IClosableIterator<IntegerEntity> range = null;
                    try {
                        range = dataSet.getNodeRankedSignatureIndexes()
                                .getRange(index, rankedSignatureCount);
                        // Fill the array with values.
                        int currentIndex = 0;
                        rsi = new int[rankedSignatureCount];
                        while (range.hasNext()) {
                            rsi[currentIndex] = range.next().getValue();
                            currentIndex++;
                        }
                    } finally {
                        if (range != null) {
                            range.close();
                        }
                    }
                }
            } finally {
                if (reader != null) {
                    pool.release(reader);
                }
            }
        }
        return rsi;
    }
}