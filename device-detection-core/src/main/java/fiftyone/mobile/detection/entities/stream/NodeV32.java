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
package fiftyone.mobile.detection.entities.stream;

import fiftyone.mobile.detection.factories.NodeFactoryShared;
import fiftyone.mobile.detection.readers.BinaryReader;
import fiftyone.properties.DetectionConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Node which can be used with the Stream data set. NumericChidren 
 * and RankedSignatureIndexes are not loaded into memory when the entity is 
 * constructed, they're only loaded from the data source when requested.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 */
public class NodeV32 extends Node {

    /**
     * A list of all the signature indexes that relate to this node.
     */
    @SuppressWarnings("VolatileArrayField")
    private volatile List<Integer> rankedSignatureIndexes;
    
    /**
     * Constructs a new instance of NodeV32.
     * 
     * @param dataSet The data set the node is contained within.
     * @param offset The offset in the data structure to the node.
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public NodeV32(fiftyone.mobile.detection.entities.stream.Dataset dataSet, 
                    int offset, BinaryReader reader) throws IOException {
        super(dataSet, offset, reader);
        super.rankedSignatureCount = reader.readUInt16();
        super.children = NodeFactoryShared.readNodeIndexesV32(
                dataSet, 
                reader, 
                (int)(offset + reader.getPos() - nodeStartStreamPosition), 
                childrenCount);
        super.numericChildrenPosition = reader.getPos();
    }

    /**
     * @return a list of all the signature indexes that relate to this node.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @Override
    @SuppressWarnings("DoubleCheckedLocking")
    public List<Integer> getRankedSignatureIndexes() throws IOException {
        List<Integer> localRankedSignatureIndexes = rankedSignatureIndexes;
        if (localRankedSignatureIndexes == null) {
            synchronized (this) {
                localRankedSignatureIndexes = rankedSignatureIndexes;
                if (localRankedSignatureIndexes == null) {
                    rankedSignatureIndexes = localRankedSignatureIndexes =
                            getRankedSignatureIndexesAsArray();
                }
            }
        }
        return localRankedSignatureIndexes;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Private methods.">
    private int getRankedSignatureIndexValue() throws IOException {
        BinaryReader reader = pool.getReader();
        try {
            // Position the reader after the numeric children.
            reader.setPos(numericChildrenPosition + (
                    ( DetectionConstants.SIZE_OF_SHORT + 
                      DetectionConstants.SIZE_OF_INT ) * 
                    getNumericChildrenLength()));
            return reader.readInt32();
        } finally {
            pool.release(reader);
        }
    }
    
    /**
     * Gets the ranked signature indexes array for the node.
     * 
     * @return An array of length _rankedSignatureCount filled with ranked 
     * signature indexes.
     */
    private List<Integer> getRankedSignatureIndexesAsArray() throws IOException {
        // RankedSignatureIndexes (RSI).
        List<Integer> rsi = null;
        if (rankedSignatureCount == 0) {
            rsi = new ArrayList<Integer>(0);
        } else {
            int rankedSignatureValue = getRankedSignatureIndexValue();

            if (rankedSignatureCount == 1) {
                rsi = new ArrayList<Integer>();
                rsi.add(rankedSignatureValue);
            } else {
                rsi = dataSet.getNodeRankedSignatureIndexes().getRange(
                        rankedSignatureValue, rankedSignatureCount);
            }
        }
        return rsi;
    }
    // </editor-fold>
}