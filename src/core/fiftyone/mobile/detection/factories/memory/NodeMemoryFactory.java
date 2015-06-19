/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fiftyone.mobile.detection.factories.memory;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.memory.Node;
import fiftyone.mobile.detection.factories.NodeFactory;
import fiftyone.mobile.detection.readers.BinaryReader;

/**
 */
public class NodeMemoryFactory extends NodeFactory{

    @Override
    protected Node construct(Dataset dataSet, int index, BinaryReader reader) {
        return new fiftyone.mobile.detection.entities.memory.Node(dataSet, index, reader);
    }
    
}
