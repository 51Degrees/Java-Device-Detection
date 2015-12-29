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
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A node index contains the characters and related child nodes of the current
 * node should any of the characters match at the position.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 * <p>
 * For more information see: 
 * <a href="https://51degrees.com/support/documentation/device-detection-data-model">
 * 51Degrees pattern data model</a>.
 */
public class NodeIndex extends NodeIndexBase implements Comparable<NodeIndex> {

    /**
     * True if the value is an index to a sub string. False if the value is 1 
     * to 4 consecutive characters.
     */
    final boolean isString;
    
    /**
     * The value of the node index. Interpretation depends on IsSubString. If
     * IsSubString is true the 4 bytes represent an offset in the strings data
     * structure to 5 or more characters. If IsSubString is false the 4 bytes
     * are character values themselves where 0 values are ignored.
     */
    private final byte[] value;

    /**
     * Returns the characters related to this node index.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    byte[] getCharacters() throws IOException {
        byte[] localCharacters = characters;
        if (localCharacters == null) {
            synchronized(this) {
                localCharacters = characters;
                if (localCharacters == null) {
                    characters = localCharacters = getCharacters(
                            getDataSet(), isString, value);
                }
            }
        }
        return localCharacters;
    }
    @SuppressWarnings("VolatileArrayField")
    private volatile byte[] characters;

    /**
     * Constructs a new instance of NodeIndex
     *
     * @param dataSet The data set the node is contained within
     * @param index The index of this object in the Node
     * @param isString True if the value is an integer offset to a string, or
     * false if the value is an array of characters to be used by the node.
     * @param value Array of bytes representing an integer offset to a string,
     * or the array of characters to be used by the node.
     * @param relatedNodeOffset The offset in the list of nodes to the node the
     * index relates to
     */
    public NodeIndex(Dataset dataSet, int index, boolean isString,
            byte[] value, int relatedNodeOffset) {
        super(dataSet, index, relatedNodeOffset);
        this.isString = isString;
        this.value = value;
    }

    /**
     * Called after the entire data set has been loaded to ensure any further
     * initialisation steps that require other items in the data set can be
     * completed.
     */
    @Override
    void init() throws IOException {
        super.init();
        if (characters == null) {
            characters = getCharacters(getDataSet(), isString, value);
        }
    }

    /**
     * Returns the characters the node index relates to.
     */
    private static byte[] getCharacters(Dataset dataSet,
            boolean isString, byte[] value) throws IOException {
        if (isString) {
            return dataSet.strings.get(
                    Integer.reverseBytes(ByteBuffer.wrap(value).getInt())).value;
        } else {
            return value;
        }
    }

    /**
     * Compares a byte array of characters at the position provided to the array
     * of characters for this node.
     *
     * @param other Array of characters to compare
     * @param startIndex The index in the other array to the required characters
     * @return The relative position of the node in relation to the other array
     */
    int compareTo(byte[] other, int startIndex) throws IOException {
        byte[] c = getCharacters();
        for (int i = c.length - 1, o = startIndex + c.length - 1; i >= 0; i--, o--) {
            int difference = c[i] - other[o];
            if (difference != 0) {
                return difference;
            }
        }
        return 0;
    }

    /**
     * Compares this node index to another.
     *
     * @param other The node index to compare
     * @return Indication of relative value based on ComponentId field
     */
    @Override
    public int compareTo(NodeIndex other) {
        try {
            return compareTo(other.getCharacters(), 0);
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * Converts the node index into a string.
     *
     * @return a string representation of the node characters
     */
    @Override
    public String toString() {
        try {
            return String.format("%s[%d]]",
                    new String(getCharacters()),
                    relatedNodeOffset);
        } catch (IOException e) {
            return super.toString();
        }
    }
}
