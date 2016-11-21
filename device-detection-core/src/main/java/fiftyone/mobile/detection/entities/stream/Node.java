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

import fiftyone.mobile.detection.entities.NodeNumericIndex;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;

/**
 * Represents a Entities.Node which can be used with the 
 * Stream data set. NumericChidren and RankedSignatureIndexes are not loaded
 * into memory when the entity is constructed, they're only loaded from the
 * data source when requested.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 */
public abstract class Node extends fiftyone.mobile.detection.entities.Node {
    
    /**
     * The position in the data set where the NumericChildren start.
     */
    protected int numericChildrenPosition;
    
    /**
     * Pool used to load NumericChildren and RankedSignatureIndexes.
     */
    protected final Pool pool;

    /**
     * Constructs a new instance of Node.
     * 
     * @param dataSet The data set the node is contained within.
     * @param offset The offset in the data structure to the node.
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public Node(Dataset dataSet, int offset, BinaryReader reader) 
                                                            throws IOException {
        super(dataSet, offset, reader);
        this.pool = dataSet.pool;
    }
    
    /**
     * An array of all the numeric children.
     * 
     * @return array of all the numeric children.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @Override
    @SuppressWarnings("DoubleCheckedLocking")
    public final NodeNumericIndex[] getNumericChildren() throws IOException {
        NodeNumericIndex[] result = numericChildren;
        if(result == null) {
            synchronized(this) {
                result = numericChildren;
                if(result == null) {
                    BinaryReader reader = pool.getReader();
                    try {
                        reader.setPos(numericChildrenPosition);
                        result = numericChildren = readNodeNumericIndexes(
                                dataSet, reader, numericChildrenCount);
                    }
                    finally {
                        pool.release(reader);
                    }
                }
            }
        }
        return result;
    }
    @SuppressWarnings("VolatileArrayField")
    protected volatile NodeNumericIndex[] numericChildren;
}
