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
package fiftyone.mobile.detection.entities;

import fiftyone.mobile.detection.Dataset;
import java.io.IOException;

/**
 * Represents a child of a node with a numeric value rather than character
 * values. Used to support the Numeric matching method if an exact match can't
 * be found.
 */
public class NodeNumericIndex extends BaseEntity {

    /**
     * The node offset which relates to this sequence of characters.
     */
    final int RelatedNodeOffset;

    /**
     * @return The numeric value of the index.
     */
    int getValue() {
        return super.getIndex();
    }

    /**
     * @return The node the numeric index relates to.
     * @throws IOException
     */
    @SuppressWarnings("DoubleCheckedLocking")
    Node getNode() throws IOException {
        Node localNode = _node;
        if (localNode == null) {
            synchronized (this) {
                localNode = _node;
                if (localNode == null) {
                    _node = localNode = getDataSet().nodes.get(RelatedNodeOffset);
                }
            }
        }
        return localNode;
    }
    private volatile Node _node;

    /**
     *
     * @param dataSet The data set the node is contained within
     * @param value The value of the numeric index. Added to it's index field.
     * @param relatedNodeOffset The offset in the list of nodes to the node the
     * index relates to
     */
    NodeNumericIndex(Dataset dataSet, short value, int relatedNodeOffset) {
        super(dataSet, value);
        RelatedNodeOffset = relatedNodeOffset;
    }
}
