/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
 * Caversham, Reading, Berkshire, United Kingdom RG4 7BY
 * 
 * This Source Code Form is the subject of the following patents and patent
 * applications, owned by 51Degrees Mobile Experts Limited of 5 Charlotte
 * Close, Caversham, Reading, Berkshire, United Kingdom RG4 7BY: 
 * European Patent No. 2871816;
 * European Patent Application No. 17184134.9;
 * United States Patent Nos. 9,332,086 and 9,350,823; and
 * United States Patent Application No. 15/686,066.
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

import fiftyone.mobile.detection.IndirectDataset;
import fiftyone.mobile.detection.entities.Modes;

import java.io.IOException;
import java.util.Date;

/**
 * A data set returned from the stream factory which includes a pool of
 * data readers that are used to fetch data from the source when the data
 * set is used to retrieve data not already in memory.
 * <p>
 * Extends {@link fiftyone.mobile.detection.Dataset}
 * <p>
 * Created by {@link fiftyone.mobile.detection.factories.StreamFactory}.
 * Since stream works with file directly a pool of readers is maintained until
 * the dataset is closed. Class provides extra methods to check how many readers
 * were created and how many are currently free to use.
 *
 * Use {@link IndirectDataset} instead, if you need access to the pool
 * of readers, or use {@link fiftyone.mobile.detection.Dataset} if not.
 * This is only here for backwards compatibility
 */
public class Dataset extends IndirectDataset {


    /**
     * Creates a dataset object with a pool of readers used to retrieve data 
     * from the data file. Only useful in stram mode.
     * 
     * @param lastModified Date and time the source data was last modified.
     * @param fileName Valid path to the uncompressed data set file.
     * @param mode Mode The mode of operation the data set will be using.
     * @param isTempFile True if the file should be deleted when the source is 
     * disposed
     * @throws IOException if there was a problem accessing data file.
     */
    public Dataset(String fileName, Date lastModified, 
                   Modes mode, boolean isTempFile) throws IOException {
        super(fileName, lastModified, mode, isTempFile);
    }
    
    /**
     * Creates a dataset object with a pool of readers used to retrieve data 
     * from the data file represented as an array of bytes. Only useful in 
     * stram mode.
     * 
     * @param data array of bytes to read from.
     * @param mode The mode of operation the data set will be using.
     * @throws IOException if there was a problem accessing data file.
     */
    public Dataset(byte[] data, Modes mode) throws IOException {
        super(data, mode);
    }
}
