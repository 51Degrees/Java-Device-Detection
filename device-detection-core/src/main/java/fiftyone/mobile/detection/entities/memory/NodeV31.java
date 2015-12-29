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
import fiftyone.mobile.detection.entities.BaseEntity;
import fiftyone.mobile.detection.factories.NodeFactoryShared;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.util.List;

/**
 * All data is loaded into memory when the entity is constructed. Implements 
 * memory node of version 3.1.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 * <p>
 * For more information see:
 * <a href="https://51degrees.com/support/documentation/pattern">
 * how Pattern device detection works</a>.
 */
public class NodeV31 extends Node{
    /**
     * An array of the ranked signature indexes for the node.
     */
    private final List<Integer> rankedSignatureIndexes;
    
    /**
     * Constructs a new instance of NodeV31.
     * 
     * @param dataSet The data set the node is contained within.
     * @param offset The offset in the data structure to the node.
     * @param reader BinaryReader connected to the source data structure and 
     *               positioned to start reading.
     */
    public NodeV31(Dataset dataSet, int offset, BinaryReader reader) {
        super(dataSet, offset, reader);
        super.rankedSignatureCount = reader.readInt32();
        super.children = NodeFactoryShared.readNodeIndexesV31(
                dataSet, 
                reader, 
                (int)(offset + reader.getPos() - nodeStartStreamPosition), 
                childrenCount);
        numericChildren = readNodeNumericIndexes(
                dataSet, reader, numericChildrenCount);
        rankedSignatureIndexes = BaseEntity.readIntegerList(
                reader, 
                rankedSignatureCount);
    }
    
    /**
     * An array of the ranked signature indexes for the node.
     * 
     * @return An array of the ranked signature indexes for the node.
     */
    @Override
    public List<Integer> getRankedSignatureIndexes() {
        return rankedSignatureIndexes;
    }
}
