/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fiftyone.mobile.detection.entities.memory;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.BaseEntity;
import fiftyone.mobile.detection.entities.NodeNumericIndex;
import fiftyone.mobile.detection.readers.BinaryReader;

/**
 * All data is loaded into memory when the entity is constructed.
 */
public class Node extends fiftyone.mobile.detection.entities.Node {
    /**
     * An array of all the numeric children.
     */
    private NodeNumericIndex[] numericChildren;
    /**
     * A list of all the signature indexes that relate to this node.
     */
    private int[] rankedSignatureIndexes;
    
    public Node(Dataset dataSet, int offset, BinaryReader reader) {
        super(dataSet, offset, reader);
        super.numericChildren = readNodeNumericIndexes(dataSet, reader, super.numericChildrenCount);
        rankedSignatureIndexes = BaseEntity.readIntegerArray(reader, super.signatureCount);
    }

    /**
     * An array of all the numeric children.
     * @return An array of all the numeric children.
     */
    @Override
    public NodeNumericIndex[] getNumericChildren() {
        return numericChildren;
    }
    
    /**
     * A list of all the signature indexes that relate to this node.
     * @return A list of all the signature indexes that relate to this node.
     */
    @Override
    public int[] getRankedSignatureIndexes() {
        return rankedSignatureIndexes;
    }
    
}
