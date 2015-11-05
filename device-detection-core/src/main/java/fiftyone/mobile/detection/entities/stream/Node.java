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
 */
public abstract class Node extends fiftyone.mobile.detection.entities.Node {
    
    /**
     * Pool used to load NumericChildren and RankedSignatureIndexes.
     */
    protected final Pool pool;

    /**
     * Constructs a new instance of Node.
     * @param dataSet The data set the node is contained within.
     * @param offset The offset in the data structure to the node.
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     * @throws java.io.IOException if there was a problem reading from the data 
     * file.
     */
    public Node(Dataset dataSet, int offset, BinaryReader reader) 
                                                            throws IOException {
        super(dataSet, offset, reader);
        this.pool = dataSet.pool;
        this.numericChildrenPosition = reader.getPos();
        getNumericChildren();
    }
    
    /**
     * An array of all the numeric children.
     * @return array of all the numeric children.
     * @throws java.io.IOException
     */
    @Override
    @SuppressWarnings("DoubleCheckedLocking")
    public final NodeNumericIndex[] getNumericChildren() throws IOException {
        if(super.numericChildren == null) {
            synchronized(this) {
                if(super.numericChildren == null) {
                    BinaryReader reader = pool.getReader();
                    reader.setPos(numericChildrenPosition);
                    super.numericChildren = readNodeNumericIndexes(dataSet, 
                                            reader, numericChildrenCount);
                    pool.release(reader);
                }
            }
        }
        return numericChildren;
    }
    protected final int numericChildrenPosition;
}
