package fiftyone.mobile.detection.factories;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.NodeIndex;
import fiftyone.mobile.detection.readers.BinaryReader;
import fiftyone.properties.DetectionConstants;

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
 * A factory that provides the common components for Node v 3.1 and 3.2.
 */
public class NodeFactoryShared {
    /**
     * The length of a node index in V3.1 format data.
     * Equivalent to sizeof(bool) + sizeof(int) + sizeof(int) in C#.
     */
    private static final int NODE_INDEX_LENGTH_V31 = 
            DetectionConstants.SIZE_OF_BOOL + 
            DetectionConstants.SIZE_OF_INT + 
            DetectionConstants.SIZE_OF_INT;
    /**
     * The length of a node index in V3.2 format data.
     * Equivalent to sizeof(int) + sizeof(int) in C#.
     */
    private static final int NODE_INDEX_LENGTH_V32 = 
            DetectionConstants.SIZE_OF_INT +
            DetectionConstants.SIZE_OF_INT;
    
    /**
     * Returns the The length of a node index in V3.1 format data.
     * @return The length of a node index in V3.1 format data.
     */
    public int getNodeIndexLengthV31() {
        return NODE_INDEX_LENGTH_V31;
    }
    
    /**
     * Returns the length of a node index in V3.2 format data.
     * @return The length of a node index in V3.2 format data.
     */
    public int getNodeIndexLengthV32() {
        return NODE_INDEX_LENGTH_V32;
    }
    
    /**
     * Used by the constructor to read the variable length list of child
     * node indexes associated with the node in V3.1 format.
     * @param dataSet The data set the node is contained within.
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     * @param offset The offset in the data structure to the node.
     * @param count The number of node indexes that need to be read.
     * @return An array of child node indexes for the node.
     */
    public static NodeIndex[] readNodeIndexesV31(Dataset dataSet, 
            BinaryReader reader, int offset, int count) {
        NodeIndex[] array = new NodeIndex[count];
        offset += DetectionConstants.SIZE_OF_SHORT;
        for (int i = 0; i < array.length; i++) {
            boolean isString = reader.readBoolean();
            array[i] = new NodeIndex(
                    dataSet, 
                    offset, 
                    isString, 
                    readValue(reader, isString), 
                    reader.readInt32());
            offset += NODE_INDEX_LENGTH_V31;
        }
        return array;
    }
    
    /**
     * Used by the constructor to read the variable length list of child
     * node indexes associated with the node in V3.2 format.
     * @param dataSet The data set the node is contained within.
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     * @param offset The offset in the data structure to the node.
     * @param count The number of node indexes that need to be read.
     * @return An array of child node indexes for the node
     */
    public static NodeIndex[] readNodeIndexesV32(Dataset dataSet, 
            BinaryReader reader, int offset, int count) {
        NodeIndex[] array = new NodeIndex[count];
        offset += DetectionConstants.SIZE_OF_SHORT;
        for (int i = 0; i < array.length; i++) {
            int index = reader.readInt32();
            boolean isString = index < 0;
            array[i] = new NodeIndex(
                    dataSet, 
                    offset, 
                    isString, 
                    readValue(reader, isString), 
                    Math.abs(index));
            offset += NODE_INDEX_LENGTH_V32;
        }
        return array;
    }
    
    /**
     * Reads the value and removes any zero characters if it's a string.
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     * @param isString True if the value is a string in the strings list.
     * @return 
     */
    private static byte[] readValue(BinaryReader reader, boolean isString) {
        byte[] byteValue = reader.readBytes(DetectionConstants.SIZE_OF_INT);
        if (!isString) {
            int i;
            for (i = 0; i < byteValue.length; i++)
                if (byteValue[i] == 0) break;
            // Equivalent of Array.Resize<byte>(ref value, i); in C#.
            byte[] tempValue = new byte[i];
            System.arraycopy(byteValue, 0, tempValue, 0, i);
            byteValue = tempValue;
            tempValue = null;
        }
        return byteValue;
    }
}
