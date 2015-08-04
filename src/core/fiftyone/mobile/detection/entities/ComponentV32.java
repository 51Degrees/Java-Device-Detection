package fiftyone.mobile.detection.entities;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
 * This Source Code Form is “Incompatible With Secondary Licenses”, as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */
/**
 * Implementation of Component where HTTP Headers are provided by the data file
 * and are no longer hardcoded like V3.1.
 */
public class ComponentV32 extends Component implements Comparable<Component> {

    /**
     * List of HTTP headers that should be checked in order to perform a 
     * detection where more headers than User-Agent are available.
     */
    private String[] httpHeaders;
    /**
     * Offsets of the HTTP headers in the data file.
     */
    private final int[] httpHeaderOffsets;
    
    /**
     * Constructs a new instance of Component. Reads the string offsets to the 
     * HTTP Headers during the constructor.
     * @param dataSet The Dataset being created.
     * @param index Index of the component within the list.
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     */
    public ComponentV32(Dataset dataSet, int index, BinaryReader reader) {
        super(dataSet, index, reader);
        this.httpHeaders = null;
        this.httpHeaderOffsets = new int[reader.readUInt16()];
        for (int i = 0; i < this.httpHeaderOffsets.length; i++) {
            this.httpHeaderOffsets[i] = reader.readInt32();
        }
    }

    /**
     * List of HTTP headers that should be checked in order to perform a 
     * detection where more headers than User-Agent are available. This data 
     * is used by methods that can HTTP Header collections.
     * @return List of HTTP headers as Strings.
     */
    @Override
    public String[] getHttpheaders() {
        if (httpHeaders == null) {
            synchronized(this) {
                if (httpHeaders == null) {
                    List<String> tempList = new ArrayList<String>();
                    for (int element : httpHeaderOffsets) {
                        try {
                            tempList.add(dataSet.strings.get(element).toString());
                        } catch (IOException ex) {
                            Logger.getLogger(ComponentV32.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    httpHeaders = tempList.toArray(new String[tempList.size()]);
                    tempList.clear();
                }
            }
        }
        return httpHeaders;
    }
    
}
