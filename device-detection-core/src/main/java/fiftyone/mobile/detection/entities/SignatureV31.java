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
import java.util.ArrayList;
import java.util.Iterator;
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
public class SignatureV31 extends Signature {

    /**
     * Constructs a new instance of SignatureV31.
     * 
     * @param ds the {@link Dataset} the signature is contained within.
     * @param index The index in the data structure to the signature.
     * @param reader Reader connected to the source data structure and 
     *               positioned to start reading.
     */
    public SignatureV31(Dataset ds, int index, BinaryReader reader) {
        super(ds, index, reader);
        List<Integer> list = readPositiveAndZeroIntegers(
                reader, 
                dataSet.signatureNodesCount);
        nodeOffsets = new ArrayList<Integer>(list.size());
        Iterator<Integer> iter = list.iterator();
        for (int i = 0; iter.hasNext(); i++) {
            nodeOffsets.add(iter.next());
        }        
    }

    /**
     * Gets the rank, where a lower number means the signature is more popular, 
     * of the signature compared to other signatures.
     * 
     * @return rank of signature expressed as integer.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @Override
    @SuppressWarnings("DoubleCheckedLocking")
    public int getRank() throws IOException {
        Integer localRank = rank;
        if (localRank == null) {
            synchronized(this) {
                localRank = rank;
                if (localRank == null) {
                    rank = localRank = getSignatureRank();
                }
            }
        }
        return localRank;
    }
    protected volatile Integer rank;

    /**
     * Returns List of the node offsets the signature relates to ordered by 
     * offset of the node.
     * 
     * @return List of the node offsets the signature relates to ordered by 
     * offset of the node.
     */
    @Override
    public List<Integer> getNodeOffsets() {
        return nodeOffsets;
    }
    private final List<Integer> nodeOffsets;

    /**
     * The number of characters in the signature.
     * 
     * @return The number of characters in the signature.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @Override
    protected int getSignatureLength() throws IOException {
        Node lastNode = 
                dataSet.nodes.get(nodeOffsets.get(nodeOffsets.size() - 1));
        return lastNode.position + lastNode.getLength() + 1;
    }
    
    /**
     * Gets the signature rank by iterating through the list of signature ranks.
     * 
     * @return Rank compared to other signatures starting at 0.
     * @throws IOException if there was a problem accessing data file.
     */
    private int getSignatureRank() throws IOException {
        for (int tempRank = 0; 
                tempRank < dataSet.rankedSignatureIndexes.size(); 
                tempRank++) {
            if (dataSet.rankedSignatureIndexes.get(tempRank) == this.index)
                return tempRank;
        }
        return Integer.MAX_VALUE;
    }
}
