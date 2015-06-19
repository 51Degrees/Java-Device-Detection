/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fiftyone.mobile.detection.factories.stream;

import fiftyone.mobile.detection.entities.stream.Pool;
import fiftyone.mobile.detection.readers.BinaryReader;

/**
 * Factory used to create stream Node entities.
 */
public class NodeStreamFactory extends fiftyone.mobile.detection.factories.NodeFactory {
    /**
     * Pool for the corresponding data set used to get readers.
     */
    private final Pool pool;
    
    /**
     * Constructs a new instance of NodeStreamFactory.
     * @param pool Pool from the data set to be used when creating new entities.
     */
    public NodeStreamFactory(Pool pool) {
        this.pool = pool;
    }
    
    /**
     * Constructs a new Node entity from the offset provided.
     * @param dataSet The data set the node is contained within.
     * @param index The offset in the data structure to the node.
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     * @return A new Node entity from the data set.
     */
    @Override
    protected fiftyone.mobile.detection.entities.Node construct(fiftyone.mobile.detection.Dataset dataSet, int index, BinaryReader reader) {
        return new fiftyone.mobile.detection.entities.stream.Node((fiftyone.mobile.detection.entities.stream.Dataset)dataSet, index, reader);
    }


}
