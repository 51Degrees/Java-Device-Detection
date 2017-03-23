package fiftyone.mobile.detection.factories;

import fiftyone.mobile.detection.entities.Node;
import fiftyone.properties.DetectionConstants;

import java.io.IOException;

/**
 * @author jo
 */
public abstract class NodeFactoryV31 extends NodeFactory {
    /**
     * Returns the length of the NodeV31 entity provided.
     *
     * @param entity An entity of type Node who length is required.
     * @return The number of bytes used to store the node.
     * @throws IOException if there was a problem accessing data file.
     */
    @Override
    public int getLength(Node entity)
            throws IOException {
        return getBaseLength() + DetectionConstants.SIZE_OF_INT +
                (entity.getChildrenLength() *
                        NodeFactoryShared.getNodeIndexLengthV31()) +
                (entity.getNumericChildrenLength() *
                        getNodeNumericIndexLength()) +
                (entity.getRankedSignatureIndexes().size() *
                        DetectionConstants.SIZE_OF_INT);
    }
}
