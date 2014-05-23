package fiftyone.mobile.detection.entities;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.memory.BaseList;
import java.io.IOException;
import java.nio.ByteBuffer;

/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
/**
 * A node index contains the characters and related child nodes of the current
 * node should any of the characters match at the position.
 */
public class NodeIndex extends BaseEntity implements Comparable<NodeIndex> {

    /**
     * The node index for the sequence of characters.
     */
    final int relatedNodeOffset;
    /**
     * True if the value is an index to a sub string. False if the value is 1 to
     * 4 consecutive characters.
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
    byte[] getCharacters() throws IOException {
        if (characters != null) {
            return characters;
        }
        return getCharacters(getDataSet(), isString, value);
    }
    private byte[] characters;

    /**
     * The node this index relates to.
     */
    Node getNode() throws IOException {
        if (node != null) {
            return node;
        }

        if (getDataSet().nodes instanceof BaseList) {
            if (node == null) {
                synchronized (this) {
                    if (node == null) {
                        node = getDataSet().getNodes().get(relatedNodeOffset);
                    }
                }
            }
            return node;
        }

        return getDataSet().getNodes().get(relatedNodeOffset);
    }
    private Node node;

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
        super(dataSet, index);
        this.isString = isString;
        this.relatedNodeOffset = relatedNodeOffset;
        this.value = value;
    }

    /**
     * Called after the entire data set has been loaded to ensure any further
     * initialisation steps that require other items in the data set can be
     * completed.
     */
    void init() throws IOException {
        characters = getCharacters(getDataSet(), isString, value);
        node = getDataSet().getNodes().get(relatedNodeOffset);
    }

    /**
     * Returns the characters the node index relates to.
     *
     * @param dataSet
     * @param isString
     * @param value
     * @return
     */
    private static byte[] getCharacters(Dataset dataSet,
            boolean isString, byte[] value) throws IOException {
        if (isString) {
            return dataSet.strings.get(
                    Integer.reverseBytes(ByteBuffer.wrap(value).getInt())).value;
        } else {
            return nonZeros(value);
        }
    }

    /**
     * Returns the non zero values in the array.
     *
     * @param value An array of values.
     * @return The non zero values as an array.
     */
    private static byte[] nonZeros(byte[] value) {
        int count = 0;
        for (int i = 0; i < value.length; i++) {
            if (value[i] != 0) {
                count++;
            }
        }

        byte[] nonZeroValues = new byte[count];
        for (int i = 0, p = 0; i < value.length; i++) {
            if (value[i] != 0) {
                nonZeroValues[p] = value[i];
                p++;
                if (p > count) {
                    break;
                }
            }
        }

        return nonZeroValues;
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
        byte[] characters = getCharacters();

        for (int i = characters.length - 1, o = startIndex + characters.length - 1; i >= 0; i--, o--) {
            int difference = characters[i] - other[o];
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
            return String.format("{0}[{1}]",
                    new String(getCharacters()),
                    relatedNodeOffset);
        } catch (IOException e) {
            return super.toString();
        }
    }
}
