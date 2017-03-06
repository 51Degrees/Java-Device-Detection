/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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

/**
 * Represents a child of a node with a numeric value rather than character
 * values. Used to support the Numeric matching method if an exact match can't
 * be found.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 * <p>
 * For more information see: 
 * <a href="https://51degrees.com/support/documentation/device-detection-data-model">
 * 51Degrees pattern data model</a>.
 */
public class NodeNumericIndex extends NodeIndexBase {

    /**
     * @return The numeric value of the index.
     */
    int getValue() {
        return super.getIndex();
    }

    /**
     * Constructs a new instance of NodeNumericIndex.
     *
     * @param dataSet The data set the node is contained within.
     * @param value Array of bytes representing an integer offset to a string,
     * or the array of characters to be used by the node.
     * @param relatedNodeOffset The offset in the list of nodes to the node the
     * index relates to.
     */
    NodeNumericIndex(Dataset dataSet, short value, int relatedNodeOffset) {
        super(dataSet, value, relatedNodeOffset);
    }
}
