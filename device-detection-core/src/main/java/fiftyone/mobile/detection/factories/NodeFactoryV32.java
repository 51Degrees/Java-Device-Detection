package fiftyone.mobile.detection.factories;

import fiftyone.properties.DetectionConstants;

import java.io.IOException;

/**
 * @author jo
 */
public abstract class NodeFactoryV32 extends NodeFactory {
    /**
     * Returns the length of the NodeV32 entity provided.
     *
     * @param entity An entity of type Node who length is require.
     * @return The number of bytes used to store the node.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @Override
    public int getLength(fiftyone.mobile.detection.entities.Node entity)
                                                            throws IOException {
        return getBaseLength() +
                DetectionConstants.SIZE_OF_USHORT +
                (entity.getChildrenLength() *
                    NodeFactoryShared.getNodeIndexLengthV32()) +
                (entity.getNumericChildrenLength() *
                    getNodeNumericIndexLength()) +
                (entity.getRankedSignatureIndexes().size() == 0
                                            ? 0
                                            : DetectionConstants.SIZE_OF_INT );
    }
}
