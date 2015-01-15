package fiftyone.mobile.detection.entities;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;

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
public class Map extends BaseEntity implements Comparable<Map> {

    /**
     * The name of the map.
     *
     * @return name of the map.
     * @throws java.io.IOException indicates an I/O exception occurred
     */
    public String getName() throws IOException {
        if (name == null) {
            synchronized (this) {
                if (name == null) {
                    name = getDataSet().strings.get(nameIndex).toString();
                }
            }
        }
        return name;
    }
    private String name;
    private final int nameIndex;

    /**
     * Constructs a new instance of NodeIndex
     *
     * @param dataSet The data set the node is contained within
     * @param index The index of this object in the Node
     * @param reader BinaryReader object to be used
     */
    public Map(Dataset dataSet, int index, BinaryReader reader) {
        super(dataSet, index);
        this.nameIndex = reader.readInt32();
    }

    /**
     * Called after the entire data set has been loaded to ensure any further
     * initialisation steps that require other items in the data set can be
     * completed.
     */
    void init() throws IOException {
        getName();
    }

    /**
     * Compares this node index to another.
     *
     * @param other The node index to compare
     * @return Indication of relative value based on ComponentId field
     */
    public int compareTo(Map other) {
        if (getDataSet() == other.getDataSet()) {
            return getIndex() - other.getIndex();
        }
        try {
            return getName().compareTo(other.getName());
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
            return getName();
        } catch (IOException e) {
            return super.toString();
        }
    }
}
