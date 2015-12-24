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
import fiftyone.mobile.detection.factories.NodeFactoryShared;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * All data is loaded into memory when the entity is constructed. Implements 
 * memory Node of version 3.2.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 * <p>
 * For more information see:
 * <a href="https://51degrees.com/support/documentation/pattern">
 * how Pattern device detection works</a>.
 */
public class NodeV32 extends Node{

    /**
     * Array of ranked signature indexes for the node.
     */
    @SuppressWarnings("VolatileArrayField")
    private volatile List<Integer> rankedSignatureIndexes;
    
    private int nodeRankedSignatureValue;
    
    /**
     * Constructs a new instance of NodeV32.
     * 
     * @param dataSet The data set the node is contained within.
     * @param offset The offset in the data structure to the node.
     * @param reader Reader connected to the source data structure and 
     *               positioned to start reading.
     */
    public NodeV32(Dataset dataSet, int offset, BinaryReader reader) {
        super(dataSet, offset, reader);
        super.rankedSignatureCount = reader.readUInt16();
        super.children = NodeFactoryShared.readNodeIndexesV32(
                dataSet, 
                reader, 
                (int)(offset + reader.getPos() - nodeStartStreamPosition), 
                childrenCount);  
        super.numericChildren = readNodeNumericIndexes(
                dataSet, reader, numericChildrenCount);
        if (rankedSignatureCount > 0) {
            nodeRankedSignatureValue = reader.readInt32();
        }
    }
    
    /**
     * Loads all the ranked signature indexes for the node.
     * <p>
     * This method should not be called as it is part of the internal logic.
     * 
     * @throws java.io.IOException if there was a problem accessing data file.
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
     * 
     * @return ranked signature indexes as array.
     * @throws IOException if there was a problem accessing data file.
     */
    private List<Integer> getRankedSignatureIndexesAsArray() throws IOException {
        List<Integer> rsi = null;
        if (rankedSignatureCount == 0) {
            rsi = new ArrayList<Integer>();
        } else if (rankedSignatureCount == 1) {
            rsi = new ArrayList<Integer>();
            // The value of _nodeRankedSignatureIndex is the ranked signature
            // index when the node only relates to 1 signature.
            rsi.add(nodeRankedSignatureValue);
        } else if (rankedSignatureCount > 1) {
            // Where the node relates to multiple signatures the 
            // _nodeRankedSignatureIndex relates to the first ranked signature 
            // index in DataSet.NodeRankedSignatureIndexes.
            rsi = dataSet.nodeRankedSignatureIndexes.
                    getRange(nodeRankedSignatureValue, rankedSignatureCount);
        }
        return rsi;
    }

    /**
     * Returns the ranked signature indexes. {@link #init()} method is called 
     * if the array has not been initialised.
     * 
     * @return the ranked signature indexes.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @Override
    @SuppressWarnings("DoubleCheckedLocking")
    public List<Integer> getRankedSignatureIndexes() throws IOException {
        List<Integer> localRankedSignatureIndexes = rankedSignatureIndexes;
        if (localRankedSignatureIndexes == null) {
            synchronized(this) {
                localRankedSignatureIndexes = rankedSignatureIndexes;
                if (localRankedSignatureIndexes == null) {
                    rankedSignatureIndexes = localRankedSignatureIndexes = 
                            getRankedSignatureIndexesAsArray();
                }
            }
        }
        return localRankedSignatureIndexes;
    }
}
