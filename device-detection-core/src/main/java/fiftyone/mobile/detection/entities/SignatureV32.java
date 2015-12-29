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
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;
import java.util.List;

/**
 * Extends {@link Signature} to provide implementation for the abstract methods.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic. Use the relevant {@link Dataset} method to access these 
 * objects.
 * <p>
 * For more information see: 
 * <a href="https://51degrees.com/support/documentation/device-detection-data-model">
 * 51Degrees pattern data model</a>.
 */
public class SignatureV32 extends Signature {
    
    /**
     * List of the node offsets the signature relates to ordered by 
     * offset of the node.
     */
    private volatile List<Integer> nodeOffsets;
    
    /**
     * The rank of the signature.
     */
    private final int rank;
    
    /**
     * The index in the "DataSet.SignatureNodeOffsets list of the first
     * node associated with this signature.
     */
    private final int firstNodeOffsetIndex;
    
    /**
     * Flags used to provide extra details about the signature.
     * Will be used in a future version of the API.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    private final byte flags;
    
    /**
     * The number of nodes associated with the signature.
     */
    private final byte nodeCount;
    
    /**
     * Constructs a new instance of SignatureV32.
     * 
     * @param dataSet the {@link Dataset} the signature is contained within.
     * @param index the index in the data structure to the signature.
     * @param reader Reader connected to the source data structure and 
     *               positioned to start reading.
     */
    public SignatureV32(Dataset dataSet, int index, BinaryReader reader) {
        super(dataSet, index, reader);
        this.nodeCount = reader.readByte();
        this.firstNodeOffsetIndex = reader.readInt32();
        this.rank = reader.readInt32();
        this.flags = reader.readByte();
    }

    /**
     * Gets the rank, where a lower number means the signature is more popular, 
     * of the signature compared to other signatures.
     * 
     * @return rank, where a lower number means the signature is more popular,
     *         of the signature compared to other signatures.
     */
    @Override
    public int getRank() {
        return this.rank;
    }

    /**
     * Returns List of the node offsets the signature relates to ordered by 
     * offset of the node.
     * 
     * @return List of the node offsets the signature relates to ordered by 
     *              offset of the node.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @Override
    @SuppressWarnings("DoubleCheckedLocking")
    public List<Integer> getNodeOffsets() throws IOException {
        List<Integer> localNodeOffsets = this.nodeOffsets;
        if (localNodeOffsets == null) {
            synchronized(this) {
                localNodeOffsets = this.nodeOffsets;
                if (localNodeOffsets == null) {
                    localNodeOffsets = this.nodeOffsets =
                            dataSet.getSignatureNodeOffsets().getRange(
                                            firstNodeOffsetIndex, nodeCount);
                }
            }
        }
        return localNodeOffsets;
    }

    /**
     * The number of characters in the signature.
     * 
     * @return The number of characters in the signature.
     */
    @Override
    protected int getSignatureLength() {
        try {
            Node lastNode = dataSet.nodes.get(dataSet.signatureNodeOffsets
                            .get(nodeCount + firstNodeOffsetIndex - 1));
            return lastNode.position + lastNode.getLength() + 1;
        } catch (IOException ex) {
            return -1;
        }
    } 
}
