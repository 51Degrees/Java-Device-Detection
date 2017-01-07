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
package fiftyone.mobile.detection.factories;

import fiftyone.mobile.detection.DatasetBuilder;
import fiftyone.mobile.detection.entities.stream.StreamDataset;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * StreamFactory will at some point be replaced by {@link DatasetBuilder}
 * <p>
 * Factory class used to create a DataSet from a source data structure.
 * <p>
 * All the entities are held in the persistent store and only loads into memory
 * when required. A cache mechanism is used to improve efficiency as many
 * entities are frequently used in a high volume environment.
 * <p>
 * The data set will be initialised very quickly as only the header information 
 * is loaded. Entities are then created when requested by the detection process 
 * and stored in a cache to avoid being recreated if their requested again after 
 * a short period of time.
 * <p>
 * The very small data structures RootNodes, Properties and Components are 
 * always stored in memory as there is no benefit retrieving them every time 
 * they're needed.
 * <p>
 * A dataset can be created in several ways:
 * <ul>
 *  <li>Using a data file:
 *  <p><code>Dataset ds = StreamFactory.create("path_to_file", false);</code>
 *  <p>Where the boolean flag indicates if the data file should or should not 
 *  be deleted when close() is invoked.
 *  <li>Using a byte array:
 *  <p><code>Dataset ds = StreamFactory.create(dataFileAsByteArray);</code>
 *  <p>Where the byte array is the 51Degrees device data file read into a byte
 *  array.
 * </ul>
 * @see DatasetBuilder for a more flexible way of creating Datastores
 */
public final class StreamFactory {

    /**
     * Constructor creates a new dataset from the supplied bytes array.
     * 
     * @param data a byte array containing the data file.
     * @return Stream Dataset object.
     * @throws IOException if there was a problem accessing data file.
     */
    public static StreamDataset create(byte[] data) throws IOException {
        return new DatasetBuilder()
                .stream()
                .addDefaultCaches()
                .build(data);
    }
    
    /**
     * Creates a new DataSet from the file provided. The last modified date of
     * the data set is the last write time of the data file provided.
     * @param filePath Uncompressed file containing the data for the data set.
     * @return A DataSet configured to read entities from the file path when
     *         required.
     * @throws IOException  if there was a problem accessing the data file.
     */
    public static StreamDataset create(String filePath)
            throws IOException {
        return create(filePath, false);
    }

    /**
     * Creates a new DataSet from the file provided. The last modified date of 
     * the data set is the last write time of the data file provided.
     * 
     * @param filePath Uncompressed file containing the data for the data set.
     * @param isTempFile True if the file should be deleted when the source is 
     *                   disposed
     * @return A DataSet configured to read entities from the file path when 
     *         required.
     * @throws IOException if there was a problem accessing data file.
     */
    public static StreamDataset create(String filePath, boolean isTempFile)
                                                            throws IOException {
        return create(filePath, 
                new Date(new File(filePath).lastModified()), 
                isTempFile);
    }

    /**
     * Constructor creates a new dataset from the supplied data file.
     * 
     * @param filepath name of the file (with path to file) to load data from.
     * @param lastModified Date and time the source data was last modified.
     * @param isTempFile True if the file should be deleted when the source is 
     * disposed.
     * @return Stream Dataset object.
     * @throws IOException if there was a problem accessing data file.
     */
    public static StreamDataset create(String filepath, Date lastModified,
            boolean isTempFile) throws IOException {

        return new DatasetBuilder()
                .stream()
                .addDefaultCaches()
                .setTempfile(isTempFile)
                .lastModified(lastModified)
                .build(filepath);
    }
}
