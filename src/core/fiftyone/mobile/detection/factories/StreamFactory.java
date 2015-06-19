package fiftyone.mobile.detection.factories;

import fiftyone.mobile.detection.entities.stream.Dataset;
import fiftyone.mobile.detection.entities.AsciiString;
import fiftyone.mobile.detection.entities.Component;
import fiftyone.mobile.detection.entities.Map;
import fiftyone.mobile.detection.entities.Node;
import fiftyone.mobile.detection.entities.Profile;
import fiftyone.mobile.detection.entities.ProfileOffset;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.entities.RankedSignatureIndex;
import fiftyone.mobile.detection.entities.Signature;
import fiftyone.mobile.detection.entities.Value;
import fiftyone.mobile.detection.entities.memory.MemoryFixedList;
import fiftyone.mobile.detection.entities.stream.StreamFixedList;
import fiftyone.mobile.detection.entities.stream.StreamVariableList;
import fiftyone.mobile.detection.factories.stream.NodeStreamFactory;
import fiftyone.mobile.detection.factories.stream.ProfileStreamFactory;
import fiftyone.mobile.detection.readers.BinaryReader;
import fiftyone.properties.DetectionConstants;
import java.io.File;
import java.io.IOException;
import java.util.Date;

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
 * Factory class used to create a DetectorDataSet from a source data structure.
 * All the entities are held in the persistent store and only loads into memory
 * when required. A cache mechanism is used to improve efficiency as many
 * entities are frequently used in a high volume environment. <p> The data set
 will be initialised very quickly as only the header information is load.
 Entities are then created when requested by the detection process and stored
 in a cache to avoid being recreated if their requested again after a short
 period of time. <p> The very small data structures RootNodes, Properties and
 * Components are always /stored in memory as there is no benefit retrieving
 * them every time they're needed. <p> For more information see
 * http://51degrees.mobi/Support/Documentation/Java
 */
public final class StreamFactory {

    /**
     * Constructor creates a new dataset from the supplied bytes array.
     * @param data a byte array containing the data file.
     * @return Stream Dataset object.
     * @throws IOException 
     */
    public static Dataset create(byte[] data) throws IOException {
        Dataset dataSet = new Dataset(data);
        load(dataSet);
        return dataSet;
    }

    /**
     * Creates a new DataSet from the file provided. The last modified date of 
     * the data set is the last write time of the data file provided.
     * @param filePath Uncompressed file containing the data for the data set.
     * @return A DataSet configured to read entities from the file path when 
     * required.
     * @throws IOException 
     */
    public static Dataset create(String filePath) throws IOException {
        File f = new File(filePath);
        if (!f.isFile() || !f.exists())
            throw new Error("Could not open the data file. Please verify the "
                    + "data file exists at the provided location.");
        Date lm = new Date(f.lastModified());
        return create(filePath, lm);
    }
    
    /**
     * Constructor creates a new dataset from the supplied data file.
     * @param filepath name of the file (with path to file) to load data from.
     * @param lastModified Date and time the source data was last modified.
     * @return Stream Dataset object.
     * @throws IOException 
     */
    public static Dataset create(String filepath, Date lastModified) throws IOException {
        Dataset dataSet = new Dataset(filepath, lastModified);
        load(dataSet);
        return dataSet;
    }

    /**
     * Uses the provided BinaryReader to load the necessary values from the data 
 file in to the Dataset. Stream mode only loads the essential information 
     * such as file headers.
     * @param reader BinaryReader to use for reading data in to the dataset.
     * @param dataSet The dataset object to load in to.
     * @return Stream Dataset object that has just been written to.
     * @throws IOException 
     */
    static void load(Dataset dataSet) throws IOException {
        BinaryReader reader = null;
        try {
            reader = dataSet.pool.getReader();
            reader.setPos(0);
            CommonFactory.loadHeader(dataSet, reader);
            
            dataSet.strings = new StreamVariableList<AsciiString>(dataSet, reader,
                    new AsciiStringFactory(), DetectionConstants.STRINGS_CACHE_SIZE);
            MemoryFixedList<Component> components = new MemoryFixedList<Component>(
                    dataSet, reader, new ComponentFactory());
            dataSet.components = components;
            MemoryFixedList<Map> maps = new MemoryFixedList<Map>(
                    dataSet, reader, new MapFactory());
            dataSet.maps = maps;
            MemoryFixedList<Property> properties = new MemoryFixedList<Property>(
                    dataSet, reader, new PropertyFactory());
            dataSet.properties = properties;
            dataSet.values = new StreamFixedList<Value>(dataSet, reader, 
                    new ValueFactory(), DetectionConstants.VALUES_CACHE_SIZE);
            dataSet.profiles = new StreamVariableList<Profile>(dataSet, reader,
                    new ProfileStreamFactory(dataSet.pool), DetectionConstants.PROFILE_CACHE_SIZE);
            dataSet.signatures = new StreamFixedList<Signature>(dataSet, reader,
                    new SignatureFactory(dataSet), 
                    DetectionConstants.SIGNATURES_CACHE_SIZE);
            dataSet.rankedSignatureIndexes = new StreamFixedList<RankedSignatureIndex>(
                    dataSet, reader, new RankedSignatureIndexFactory(), 
                    DetectionConstants.RANKED_SIGNATURE_CACHE_SIZE);
            dataSet.nodes = new StreamVariableList<Node>(dataSet, reader,
                    new NodeStreamFactory(dataSet.pool), DetectionConstants.NODES_CACHE_SIZE);
            MemoryFixedList<Node> rootNodes = new MemoryFixedList<Node>(dataSet,
                    reader, new RootNodeFactory());
            dataSet.rootNodes = rootNodes;
            dataSet.profileOffsets = new MemoryFixedList<ProfileOffset>(
                    dataSet, reader, new ProfileOffsetFactory());

            // Read into memory all the small lists which are frequently accessed.
            reader.setPos(components.header.getStartPosition());
            components.read(reader);
            reader.setPos(maps.header.getStartPosition());
            maps.read(reader);
            reader.setPos(properties.header.getStartPosition());
            properties.read(reader);
            reader.setPos(rootNodes.header.getStartPosition());
            rootNodes.read(reader);
        } finally {
            if (reader != null)
                dataSet.pool.release(reader);
        }
    }
}
