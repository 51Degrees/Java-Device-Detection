/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fiftyone.mobile.detection.entities.stream;

import fiftyone.mobile.detection.entities.BaseEntity;
import fiftyone.mobile.detection.entities.NodeNumericIndex;
import fiftyone.mobile.detection.readers.BinaryReader;

/**
 * Represents Node which can be used with the Stream data set. NumericChidren 
 * and RankedSignatureIndexes are not loaded into memory when the entity is 
 * constructed, they're only loaded from the data source when requested.
 */
public class Node extends fiftyone.mobile.detection.entities.Node {
    /**
     * The position in the data set where the NumericChildren start.
     */
    private final int position;
    /**
     * Pool used to load NumericChildren and RankedSignatureIndexes.
     */
    private final Pool pool;

    /**
     * Constructs a new instance of Node.
     * @param dataSet The data set the node is contained within.
     * @param offset The offset in the data structure to the node.
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     */
    public Node(Dataset dataSet, int offset, BinaryReader reader) {
        super(dataSet, offset, reader);
        this.pool = dataSet.pool;
        this.position = reader.getPos();
        getNumericChildren();
    }
    
    /**
     * An array of all the numeric children.
     * @return array of all the numeric children.
     */
    @Override
    public final NodeNumericIndex[] getNumericChildren() {
        if(super.numericChildren == null) {
            synchronized(this) {
                if(super.numericChildren == null) {
                    BinaryReader reader = null;
                    try {
                        reader = pool.getReader();
                        reader.setPos(position);
                        super.numericChildren = 
                                fiftyone.mobile.detection.entities.Node.readNodeNumericIndexes(dataSet, reader, 
                                        numericChildrenCount);
                    } catch(Exception ex) {
                        throw new Error("Cannot obtain numeric Children: "+ex);
                    } finally {
                        if (reader != null)
                            pool.release(reader);
                    }
                }
            }
        }
        return numericChildren;
    }
    
    /**
     * A list of all the signature indexes that relate to this node.
     * @return list of all the signature indexes that relate to this node.
     */
    @Override
    public int[] getRankedSignatureIndexes() {
        if (super.signatureIndexes == null) {
            synchronized(this) {
                if (super.signatureIndexes == null) {
                    BinaryReader reader = null;
                    try {
                        reader = pool.getReader();
                        reader.setPos(position+((2 + 4)*super.numericChildrenCount));
                        signatureIndexes = BaseEntity.readIntegerArray(reader, super.signatureCount);
                    } catch(Exception ex) {
                        throw new Error("Cannot to obtain signature indexes"+ex);
                    } finally {
                        if (reader != null)
                            pool.release(reader);
                    }
                }
            }
        }
        return signatureIndexes;
    } 
}
