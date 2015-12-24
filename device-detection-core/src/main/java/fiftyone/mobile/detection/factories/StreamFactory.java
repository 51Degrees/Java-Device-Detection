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

import fiftyone.mobile.detection.entities.stream.Dataset;
import fiftyone.mobile.detection.entities.AsciiString;
import fiftyone.mobile.detection.entities.Component;
import fiftyone.mobile.detection.entities.Map;
import fiftyone.mobile.detection.entities.Modes;
import fiftyone.mobile.detection.entities.Node;
import fiftyone.mobile.detection.entities.Profile;
import fiftyone.mobile.detection.entities.ProfileOffset;
import fiftyone.mobile.detection.entities.Signature;
import fiftyone.mobile.detection.entities.Value;
import fiftyone.mobile.detection.entities.memory.MemoryFixedList;
import fiftyone.mobile.detection.entities.memory.PropertiesList;
import fiftyone.mobile.detection.entities.stream.FixedCacheList;
import fiftyone.mobile.detection.entities.stream.IntegerList;
import fiftyone.mobile.detection.entities.stream.StreamVariableList;
import fiftyone.mobile.detection.factories.stream.NodeStreamFactoryV31;
import fiftyone.mobile.detection.factories.stream.NodeStreamFactoryV32;
import fiftyone.mobile.detection.factories.stream.ProfileStreamFactory;
import fiftyone.mobile.detection.readers.BinaryReader;
import fiftyone.properties.DetectionConstants;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Factory class used to create a DetectorDataSet from a source data structure.
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
 */
public final class StreamFactory {

    /**
     * Constructor creates a new dataset from the supplied bytes array.
     * 
     * @param data a byte array containing the data file.
     * @return Stream Dataset object.
     * @throws IOException if there was a problem accessing data file.
     */
    public static Dataset create(byte[] data) throws IOException {
        Dataset dataSet = new Dataset(data, Modes.MEMORY_MAPPED);
        load(dataSet);
        return dataSet;
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
    public static Dataset create(String filePath, boolean isTempFile) 
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
    public static Dataset create(String filepath, Date lastModified, 
            boolean isTempFile) throws IOException {
        Dataset dataSet = 
                new Dataset(filepath, lastModified, Modes.FILE, isTempFile);
        load(dataSet);
        return dataSet;
    }

    /**
     * Uses the provided BinaryReader to load the necessary values from the data 
     * file in to the Dataset. Stream mode only loads the essential information 
     * such as file headers.
     * 
     * @param reader BinaryReader to use for reading data in to the dataset.
     * @param dataSet The dataset object to load in to.
     * @return Stream Dataset object that has just been written to.
     * @throws IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("null")
    static void load(Dataset dataSet) throws IOException {
        BinaryReader reader = null;
        try {
            reader = dataSet.pool.getReader();
            reader.setPos(0);
            //Load headers that are common for both V31 and V32.
            CommonFactory.loadHeader(dataSet, reader);
            
            dataSet.strings = new StreamVariableList<AsciiString>(
                    dataSet, reader, new AsciiStringFactory(), 
                    DetectionConstants.STRINGS_CACHE_SIZE);
            
            MemoryFixedList<Component> components = null;
            switch (dataSet.versionEnum) {
                case PatternV31:
                    components = new MemoryFixedList<Component>(
                            dataSet, reader, new ComponentFactoryV31());
                    break;
                case PatternV32:
                    components = new MemoryFixedList<Component>(
                            dataSet, reader, new ComponentFactoryV32());
                    break;
            }
            dataSet.components = components;
            
            MemoryFixedList<Map> maps = new MemoryFixedList<Map>(
                    dataSet, reader, new MapFactory());
            dataSet.maps = maps;
                      
            PropertiesList properties = new PropertiesList(
                    dataSet, reader, new PropertyFactory());
            dataSet.properties = properties; 
            
            dataSet.values = new FixedCacheList<Value>(
                    dataSet, reader, new ValueFactory(), 
                    DetectionConstants.VALUES_CACHE_SIZE);
            
            dataSet.profiles = new StreamVariableList<Profile>(
                    dataSet, reader, new ProfileStreamFactory(), 
                    DetectionConstants.PROFILE_CACHE_SIZE);
            
            switch (dataSet.versionEnum) {
                case PatternV31:
                    dataSet.signatures = new FixedCacheList<Signature>(
                            dataSet, reader, new SignatureFactoryV31(dataSet), 
                            DetectionConstants.SIGNATURES_CACHE_SIZE);
                    break;
                case PatternV32:
                    dataSet.signatures = new FixedCacheList<Signature>(
                            dataSet, reader, new SignatureFactoryV32(dataSet), 
                            DetectionConstants.SIGNATURES_CACHE_SIZE);
                    dataSet.signatureNodeOffsets = 
                            new IntegerList(dataSet, reader);
                    dataSet.nodeRankedSignatureIndexes = 
                            new IntegerList(dataSet, reader);
                    break;
            }
            dataSet.rankedSignatureIndexes = new IntegerList(dataSet, reader);
            
            switch (dataSet.versionEnum) {
                case PatternV31:
                    dataSet.nodes = new StreamVariableList<Node>(
                            dataSet, reader, 
                            new NodeStreamFactoryV31(), 
                            DetectionConstants.NODES_CACHE_SIZE);
                    break;
                case PatternV32:
                    dataSet.nodes = new StreamVariableList<Node>(
                            dataSet, reader, 
                            new NodeStreamFactoryV32(), 
                            DetectionConstants.NODES_CACHE_SIZE);
                    break;
            }
            
            MemoryFixedList<Node> rootNodes = new MemoryFixedList<Node>(
                    dataSet, reader, new RootNodeFactory());
            dataSet.rootNodes = rootNodes;
            
            MemoryFixedList<ProfileOffset> profileOffsets = 
                new MemoryFixedList<ProfileOffset>( dataSet, reader, 
                                                    new ProfileOffsetFactory());
             dataSet.profileOffsets = profileOffsets;
             
            //Read into memory all small lists which are frequently accessed.
            reader.setPos(components.header.getStartPosition());
            components.read(reader);
            reader.setPos(maps.header.getStartPosition());
            maps.read(reader);
            reader.setPos(properties.header.getStartPosition());
            properties.read(reader);
            reader.setPos(rootNodes.header.getStartPosition());
            rootNodes.read(reader);
            reader.setPos(profileOffsets.header.getStartPosition());
            profileOffsets.read(reader);
            
        } finally {
            if (reader != null) {
                dataSet.pool.release(reader);
            }
        }
    }
}
