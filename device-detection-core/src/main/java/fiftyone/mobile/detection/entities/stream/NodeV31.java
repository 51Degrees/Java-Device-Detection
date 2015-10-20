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

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.NodeIndex;
import fiftyone.mobile.detection.factories.NodeFactoryShared;
import fiftyone.mobile.detection.readers.BinaryReader;
import fiftyone.properties.DetectionConstants;
import java.io.IOException;


/**
 * Represents a NodeV31 which can be used with the Stream data set. 
 * NumericChidren and RankedSignatureIndexes are not loaded into memory when 
 * the entity is constructed, they're only loaded from the data source when 
 * requested.
 */
public class NodeV31 extends Node {

    /**
     * An array of the ranked signature indexes for the node.
     */
    private volatile int[] rankedSignatureIndexes;
    
    /**
     * Constructs a new instance of NodeV31.
     * @param dataSet The data set the node is contained within.
     * @param offset The offset in the data structure to the node.
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     */
    public NodeV31(fiftyone.mobile.detection.entities.stream.Dataset dataSet, 
            int offset, BinaryReader reader) throws IOException {
        super(dataSet, offset, reader);
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

    @Override
    protected NodeIndex[] readNodeIndexes(Dataset dataSet, BinaryReader reader, 
            int offset, int count) {
        return NodeFactoryShared.readNodeIndexesV31(dataSet, reader, 
                                                    offset, count);
    }

    /**
     * @return An array of the ranked signature indexes for the node.
     */
    @Override
    public int[] getRankedSignatureIndexes() throws IOException {
        int[] localRankedSignatureIndexes = rankedSignatureIndexes;
        if (localRankedSignatureIndexes == null) {
            synchronized (this) {
                localRankedSignatureIndexes = rankedSignatureIndexes;
                if (localRankedSignatureIndexes == null) {
                    BinaryReader reader = pool.getReader();
                    reader.setPos(numericChildrenPosition + 
                        ((DetectionConstants.SIZE_OF_SHORT + 
                          DetectionConstants.SIZE_OF_INT) * 
                                getNumericChildrenLength()));
                    rankedSignatureIndexes = localRankedSignatureIndexes = 
                            readIntegerArray(reader, rankedSignatureCount);
                    pool.release(reader);
                }
            }
        }
        return localRankedSignatureIndexes;
    }
}