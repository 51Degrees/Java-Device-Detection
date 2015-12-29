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
import fiftyone.mobile.detection.entities.memory.MemoryBaseList;
import java.io.IOException;

/**
 * Base class used by all node indexes containing common functionality.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 * <p>
 * For more information see: 
 * <a href="https://51degrees.com/support/documentation/device-detection-data-model">
 * 51Degrees pattern data model</a>.
 */
public abstract class NodeIndexBase extends BaseEntity {
    
    /**
     * The node index for the sequence of characters.
     */
    protected final int relatedNodeOffset;
    
    /**
     * Constructs a new instance of NodeIndex
     *
     * @param dataSet The data set the node is contained within
     * @param index The index of this object in the Node
     * @param relatedNodeOffset The offset in the list of nodes to the node the
     * index relates to
     */    
    public NodeIndexBase(Dataset dataSet, int index, int relatedNodeOffset) {
        super(dataSet, index);
        this.relatedNodeOffset = relatedNodeOffset;
    }
    
    /**
     * The node this index relates to.
     * <p>
     * If the data set is operating in memory mode then there will only ever
     * be one instance of the associated node. Therefore double checked locking
     * can be used to retrieve this single instance and store a reference to it.
     * <p>
     * When stream mode is being used we wish to ensure that instances of unused
     * objects are freed by the garbage collector quickly. If the reference to 
     * the cached instance were retained by the NodeIndex instance then more 
     * memory would be used as the garbage collector would not recognise that 
     * it could be freed.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    Node getNode() throws IOException {
        Node result = node;
        if (result != null) {
            return result;
        } 
        
        if (getDataSet().nodes instanceof MemoryBaseList) {
            synchronized (this) {
                result = node;
                if (result == null) {
                    node = result = getDataSet().getNodes().get(
                            relatedNodeOffset);
                }
            }
            return result;
        } 
        
        return getDataSet().getNodes().get(relatedNodeOffset);
    }
    private volatile Node node;
    
    /**
     * Called after the entire data set has been loaded to ensure any further
     * initialisation steps that require other items in the data set can be
     * completed.
     */
    void init() throws IOException {
        if (node == null) {
            node = getDataSet().getNodes().get(relatedNodeOffset);
        }
    }
}
