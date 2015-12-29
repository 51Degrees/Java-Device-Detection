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
package fiftyone.mobile.detection;

import fiftyone.mobile.detection.entities.stream.TriePool;
import fiftyone.properties.DetectionConstants;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Trie provider of version 3.2.
 */
public class TrieProviderV32 extends TrieProvider {

    /**
     * Length of each property index.
     */
    private final static int PROPERTY_LENGTH = DetectionConstants.SIZE_OF_INT * 3;
    
    /**
     * Constructs a new version 3.2 instance of a Trie provider.
     * @param copyright The copyright notice for the data file.
     * @param strings Array containing all strings in the output.
     * @param httpHeaders Array of http headers.
     * @param properties Array of properties.
     * @param devices Array of devices.
     * @param lookupList Lookups data array.
     * @param nodesLength The length of the node data.
     * @param nodesOffset The position of the start of the nodes in the file provided.
     * @param pool Pool connected to the data source.
     * @throws FileNotFoundException if a file could not be found.
     */
    public TrieProviderV32(String copyright, byte[] strings, byte[] httpHeaders, 
                           byte[] properties, byte[] devices, short[] lookupList, 
                           long nodesLength, long nodesOffset, TriePool pool) 
                           throws FileNotFoundException {
        super(copyright, strings, properties, devices, lookupList, nodesLength, 
                nodesOffset, pool);
        
        int limit = _properties.array().length / PROPERTY_LENGTH;

        for (int i = 0; i < limit; i++) {
            String value = getStringValue(_properties.getInt());
            int headerCount = _properties.getInt();
            int headerFirstIndex = _properties.getInt();
            _propertyIndex.put(value, i);
            _propertyNames.add(value);
            propertyHttpHeaders.add(getHeaders(httpHeaders, headerCount, headerFirstIndex));
        }
    }

    /**
     * Returns the array of headers.
     * @param httpHeaders byte array to read headers from.
     * @param headerCount how many headers there are.
     * @param headerFirstIndex where to start reading from.
     * @return the array of headers.
     */
    private String[] getHeaders(byte[] httpHeaders, int headerCount, 
                                int headerFirstIndex) {
        List<String> headers = new ArrayList<String>();
        for (int i = 0; i < headerCount; i++) {
            int from = (headerFirstIndex + i) * DetectionConstants.SIZE_OF_INT;
            int to = from + DetectionConstants.SIZE_OF_INT;
            byte[] value = Arrays.copyOfRange(httpHeaders, from, to);
            ByteBuffer buffer = ByteBuffer.wrap(value);
            buffer.order(ByteOrder.LITTLE_ENDIAN);  // if you want little-endian
            int offset = buffer.getShort();
            headers.add(getStringValue(offset));
        }
        return headers.toArray(new String[headers.size()]);
    }
    
}
