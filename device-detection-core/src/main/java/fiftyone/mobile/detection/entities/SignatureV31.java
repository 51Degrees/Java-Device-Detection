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

/**
 * Signature of a user agent in version 3.1 data format.
 */
public class SignatureV31 extends Signature {

    /**
     * List of the node offsets the signature relates to ordered by 
     * offset of the node.
     */
    private final int[] nodeOffsets;
    /**
     * The rank of this signature.
     */
    protected volatile Integer rank;
    
    /**
     * Constructs a new instance of SignatureV31.
     * @param ds The data set the signature is contained within.
     * @param index The index in the data structure to the signature.
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     */
    public SignatureV31(Dataset ds, int index, BinaryReader reader) {
        super(ds, index, reader);
        nodeOffsets = readOffsets(ds, reader, ds.signatureNodesCount);
    }

    /**
     * Gets the rank, where a lower number means the signature is more popular, 
     * of the signature compared to other signatures.
     * @return rank of signature expressed as integer.
     */
    @Override
    public int getRank() {
        Integer localRank = rank;
        if (localRank == null) {
            synchronized(this) {
                localRank = rank;
                if (localRank == null) {
                    try {
                        rank = localRank = getSignatureRank();
                    } catch (IOException ex) {
                        //TODO: handle exception.
                    }
                }
            }
        }
        return localRank;
    }

    /**
     * Returns List of the node offsets the signature relates to ordered by 
     * offset of the node.
     * @return List of the node offsets the signature relates to ordered by 
     * offset of the node.
     */
    @Override
    public int[] getNodeOffsets() {
        return nodeOffsets;
    }

    /**
     * The number of characters in the signature.
     * @return The number of characters in the signature.
     */
    @Override
    protected int getSignatureLength() {
        try {
            Node lastNode = dataSet.nodes.get(nodeOffsets[nodeOffsets.length - 1]);
            return lastNode.position + lastNode.getLength() + 1;
        } catch (IOException ex) {
            //TODO: handle exception.
        }
        return -1;
    }
    
    /**
     * Gets the signature rank by iterating through the list of signature ranks.
     * @return Rank compared to other signatures starting at 0.
     * @throws IOException 
     */
    private int getSignatureRank() throws IOException {
        for (int rank = 0; rank < dataSet.rankedSignatureIndexes.size(); rank++) {
            if (dataSet.rankedSignatureIndexes.get(rank).value == this.index)
                return rank;
        }
        return Integer.MAX_VALUE;
    }
}
