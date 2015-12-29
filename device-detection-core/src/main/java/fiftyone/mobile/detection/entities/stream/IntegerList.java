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

package fiftyone.mobile.detection.entities.stream;

import fiftyone.mobile.detection.ISimpleList;
import fiftyone.mobile.detection.WrappedIOException;
import fiftyone.mobile.detection.entities.headers.Header;
import fiftyone.mobile.detection.readers.BinaryReader;
import fiftyone.properties.DetectionConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a way of storing integers in a list and implements utility methods 
 * such as {@link #getRange(int, int)}.
 * <p>
 * This class is part of the internal logic and should not be referenced 
 * directly.
 */
public class IntegerList implements ISimpleList {

    // Entity header.
    private final Header header;
    // Array of items contained in the list.
    protected final Dataset dataSet;
    
    /**
     * Constructs a new instance of this class.
     * 
     * @param dataSet stream dataset with pool of 
     * @param reader BinaryReader connected to the source data structure and 
     *               positioned to start reading.
     */
    public IntegerList(Dataset dataSet, BinaryReader reader) {
        this.header = new Header(reader);
        this.dataSet = dataSet;
    }
    
    @Override
    public int get(int index) {
        int result = 0;
        BinaryReader reader = null;
        try {
            reader = dataSet.pool.getReader();
            reader.setPos(header.getStartPosition() + 
                    (DetectionConstants.SIZE_OF_INT * index));
            result = reader.readInt32();
        } catch (IOException ex) {
            throw new WrappedIOException("Failed to access data file to "
                    + "retrieve a binary reader");
        } finally {
            if (reader != null) {
                dataSet.pool.release(reader);
            }
        }
        return result;
    }
    
    @Override
    public List<Integer> getRange(int index, int count) {
        List<Integer> result = new ArrayList<Integer>();
        BinaryReader reader = null;
        try {
            reader = dataSet.pool.getReader();
            reader.setPos(header.getStartPosition() + 
                    (DetectionConstants.SIZE_OF_INT * index));
            for (int i = 0; i < count; i++) {
                result.add(reader.readInt32());
            }
        } catch (IOException ex) {
            throw new WrappedIOException("Failed to access data file to "
                    + "retrieve a binary reader");
        } finally {
            if (reader != null) {
                dataSet.pool.release(reader);
            }
        }
        return result;
    }

    @Override
    public int size() {
        return header.getCount();
    }
}
