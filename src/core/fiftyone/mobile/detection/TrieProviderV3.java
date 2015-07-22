package fiftyone.mobile.detection;

import fiftyone.mobile.detection.entities.stream.TriePool;
import fiftyone.properties.DetectionConstants;
import java.io.FileNotFoundException;
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
 * Decision Trie data structure provider.
 */
public class TrieProviderV3 extends TrieProvider {

    /**
     * Constructs a new instance of a Trie provider version 3.0.
     * @param copyright The copyright notice for the data file.
     * @param strings Array containing all strings in the output.
     * @param properties Array of properties.
     * @param devices Array of devices.
     * @param lookupList Lookups data array.
     * @param nodesLength The length of the node data.
     * @param nodesOffset The position of the start of the nodes in the 
     * file provided.
     * @param pool >Pool connected to the data source.
     * @throws FileNotFoundException 
     */
    public TrieProviderV3(String copyright, byte[] strings, byte[] properties, 
            byte[] devices, short[] lookupList, long nodesLength, long nodesOffset, 
            TriePool pool) throws FileNotFoundException {
        super(copyright, strings, properties, devices, lookupList, 
                nodesLength, nodesOffset, pool);
        
        String[] headers = DetectionConstants.DEVICE_USER_AGENT_HEADERS;
        int count = _properties.array().length / DetectionConstants.SIZE_OF_INT;
        ByteBuffer bb = ByteBuffer.wrap(_properties.array());
        for (int i = 0; i < count; i++) {
            String value = getStringValue(bb.getInt());
            _propertyIndex.put(value, i);
            _propertyNames.add(value);
            propertyHttpHeaders.add(headers);
        }
    }

}
