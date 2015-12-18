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

import java.io.FileInputStream;
import java.io.IOException;

import fiftyone.mobile.detection.Dataset;
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
import fiftyone.mobile.detection.entities.memory.MemoryIntegerList;
import fiftyone.mobile.detection.entities.memory.MemoryVariableList;
import fiftyone.mobile.detection.entities.memory.PropertiesList;
import fiftyone.mobile.detection.factories.memory.NodeMemoryFactoryV31;
import fiftyone.mobile.detection.factories.memory.NodeMemoryFactoryV32;
import fiftyone.mobile.detection.factories.memory.ProfileMemoryFactory;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.File;
import java.util.Date;

/**
 * Factory class used to create a DataSet from a binary device data file.
 * All the entities are held in memory and the source data file is not
 * referenced once the data set is created. 
 * <p>
 * The memory usage of the resulting data set following initialisation will be 
 * consistent. The performance of the data set will be very fast compared to 
 * the stream based implementation as all required data is loaded into memory 
 * and references between related objects set at initialisation. However 
 * overall memory usage will be higher than the stream based implementation on 
 * lightly loaded environments.
 * <p>
 * Initialisation may take several seconds depending on system performance. 
 * Initialisation calculates all the references between entities. 
 * If initialisation is not performed then references will be calculated when 
 * needed. As such avoiding initialisation improves the time taken to create the
 * data set, at the expense of performance for the initial detections. The
 * default setting is not to initialise the data set.
 * <p>
 * Create a fully initialised Dataset:
 * <code>Dataset ds = MemoryFactory.create("path_to_file", true);</code>
 * <br>
 * The resulting dataset can then be used to work with data or used in a 
 * Provider to perform device detections. Use in provider like:
 * <code>Provider p = new Provider(ds);</code>
 * <p>
 * This is a factory that creates a memory resident version of the 51Degrees 
 * device data in the form of a Dataset object.
 */
public class MemoryFactory {
    /**
     * Creates a new Dataset from the byte array.
     * 
     * @param data Array of bytes to build the data set from.
     * @return A Dataset filled with data from the array.
     * @throws IOException if there was a problem accessing data file.
     */
    public static Dataset create(byte[] data) throws IOException {
        return create(data, false);
    }
    /**
     * Creates a new Dataset from the byte array.
     * 
     * @param data Array of bytes to build the data set from.
     * @param init True to indicate that the data set should be filling 
     *             initialised..
     * @return filled with data from the array.
     * @throws IOException if there was a problem accessing data file.
     */
    public static Dataset create(byte[] data, boolean init) throws IOException {
        Dataset dataSet = new Dataset(new Date(Long.MIN_VALUE), Modes.MEMORY);
        BinaryReader reader = new BinaryReader(data);
        load(dataSet, reader, init);
        return dataSet;
    }
    
    /**
     * Creates a new DataSet from the file provided. The last modified date of 
     * the data set is the last write time of the data file provided.
     * 
     * @param filename Uncompressed file containing the data for the data set.
     * @return filled with data from the array.
     * @throws IOException if there was a problem accessing data file.
     */
    public static Dataset create(String filename) throws IOException {
        File f = new File(filename);
        if (!f.exists() || !f.isFile())
            throw new IllegalArgumentException("Could not construct a dataset "
                    + "for MemoryProvider. The path you have provided is "
                    + "either a directory or does not exist: "+filename);
        Date lm = new Date(f.lastModified());
        return create(filename, false, lm);
    }
    
    /**
     * Creates a new DataSet from the file provided.
     * 
     * @param filename Uncompressed file containing the data for the data set.
     * @param init True to indicate that the data set should be 
     *             fully initialised.
     * @return A DataSet filled with data from the array.
     * @throws IOException if there was a problem accessing data file.
     */
    public static Dataset create(String filename, boolean init) 
                                                            throws IOException {
        File f = new File(filename);
        if (!f.exists() || !f.isFile())
            throw new IllegalArgumentException("Could not construct a dataset "
                    + "for MemoryProvider. The path you have provided is "
                    + "either a directory or does not exist: "+filename);
        Date lm = new Date(f.lastModified());
        return create(filename, init, lm);
    }
    
    /**
     * Creates a new Dataset from the file provided.
     * 
     * @param filename Uncompressed file containing the data for the data set.
     * @param init True to indicate that the data set should be filling 
     *             initialised.
     * @param lastModified Date and time the source data was last modified.
     * @return filled with data from the array.
     * @throws IOException if there was a problem accessing data file.
     */
    public static Dataset create(String filename, boolean init,
                                 Date lastModified) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(filename);
        try {
            return create(fileInputStream, init, lastModified);
        } finally {
            fileInputStream.close();
        }
    }

    /**
     * Creates a lazily initialised {@link Dataset} from a 
     * {@link FileInputStream}.
     * <p>
     * It is the caller's responsibility to close the passed fileInputStream.
     *
     * @param fileInputStream Source of data.
     * @return Uninitialised Dataset.
     * @throws IOException if there was a problem accessing data file.
     */
    public static Dataset create(FileInputStream fileInputStream)
            throws IOException {
        return create(fileInputStream, false);
    }

    /**
     * Creates an optionally initialised {@link Dataset} from a 
     * {@link FileInputStream}.
     * <p>
     * It is the caller's responsibility to close the passed fileInputStream
     * <p>
     * Initialisation increases load time and initial memory footprint,
     * but improves run time performance.
     *
     * @param fileInputStream Source of data.
     * @param init preemptive initialise if true.
     * @return Optionally initialised Dataset.
     * @throws IOException if there was a problem accessing data file.
     */
    public static Dataset create(FileInputStream fileInputStream, boolean init)
            throws IOException {
        return create(fileInputStream, init, new Date (Long.MIN_VALUE));
    }

    /**
     * Creates an optionally initialised {@link Dataset} from a 
     * {@link FileInputStream}.
     * <p>
     * It is the caller's responsibility to close the passed fileInputStream
     * <p>
     * Initialisation increases load time and initial memory footprint,
     * but improves run time performance.
     *
     * @param fileInputStream Source of data.
     * @param init preemptive initialise if true.
     * @param lastModified the date of data update.
     * @return Optionally initialised Dataset.
     * @throws IOException if there was a problem accessing data file.
     */
    public static Dataset create(FileInputStream fileInputStream, boolean init,
                Date lastModified) throws IOException {
        Dataset dataSet = new Dataset(lastModified, Modes.MEMORY);
        BinaryReader reader = new BinaryReader(fileInputStream);
        load(dataSet, reader, init);
        return dataSet;
    }

    /**
     * Creates a new DataSet from the binary reader provided.
     * <p>
     * A DataSet is constructed using the reader to retrieve the 
     * header information. This is then passed to the Read methods to 
     * create the lists before reading the data into memory. Finally it 
     * initialise is required references between entities are worked out 
     * and stored.
     * 
     * @param dataSet The data set to be loaded with data from the reader.
     * @param reader BinaryReader connected to the source data structure and 
     *               positioned to start reading.
     * @param init True to indicate that the data set should be fully 
     *             initialised.
     * @throws IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("null")
    public static void load(Dataset dataSet, BinaryReader reader, boolean init) 
            throws IOException {
        CommonFactory.loadHeader(dataSet, reader);

        MemoryVariableList<AsciiString> strings = 
                new MemoryVariableList<AsciiString>(dataSet, reader, 
                                                    new AsciiStringFactory());
        
        MemoryFixedList<Component> components = null;
        switch(dataSet.versionEnum) {
            case PatternV31:
                components = new MemoryFixedList<Component>(
                        dataSet, reader, new ComponentFactoryV31());
                break;
            case PatternV32:
                components = new MemoryFixedList<Component>(
                        dataSet, reader, new ComponentFactoryV32());
        }
        
        MemoryFixedList<Map> maps = new MemoryFixedList<Map>(
                dataSet, reader, new MapFactory());
        PropertiesList properties = new PropertiesList(
                dataSet, reader, new PropertyFactory());
        MemoryFixedList<Value> values = new MemoryFixedList<Value>(dataSet,
                reader, new ValueFactory());
        MemoryVariableList<Profile> profiles = new MemoryVariableList<Profile>(
                dataSet, reader, new ProfileMemoryFactory());
        
        MemoryFixedList<Signature> signatures = null;
        MemoryIntegerList signatureNodeOffsets = null;
        MemoryIntegerList nodeRankedSignatureIndexes = null;
        switch(dataSet.versionEnum) {
            case PatternV31:
                signatures = new MemoryFixedList<Signature>(
                        dataSet, reader, new SignatureFactoryV31(dataSet));
                break;
            case PatternV32:
                signatures = new MemoryFixedList<Signature>(
                        dataSet, reader, new SignatureFactoryV32(dataSet));
                signatureNodeOffsets = new MemoryIntegerList(reader);
                nodeRankedSignatureIndexes = new MemoryIntegerList(reader);
                break;
        }
        MemoryIntegerList rankedSignatureIndexes = new MemoryIntegerList(reader);
        
        MemoryVariableList<Node> nodes = null;
        switch (dataSet.versionEnum) {
            case PatternV31:
                nodes = new MemoryVariableList<Node>(
                        dataSet, reader, new NodeMemoryFactoryV31());
                break;
            case PatternV32:
                nodes = new MemoryVariableList<Node>(
                        dataSet, reader, new NodeMemoryFactoryV32());
                break;
        }
        
        MemoryFixedList<Node> rootNodes = new MemoryFixedList<Node>(dataSet,
                reader, new RootNodeFactory());
        MemoryFixedList<ProfileOffset> profileOffsets = 
                new MemoryFixedList<ProfileOffset>( dataSet, reader, 
                                                    new ProfileOffsetFactory());

        dataSet.strings = strings;
        dataSet.components = components;
        dataSet.maps = maps;
        dataSet.properties = properties;
        dataSet.values = values;
        dataSet.profiles = profiles;
        dataSet.signatures = signatures;
        dataSet.rankedSignatureIndexes = rankedSignatureIndexes;
        
        switch(dataSet.versionEnum) {
            case PatternV32:
                dataSet.signatureNodeOffsets = signatureNodeOffsets;
                dataSet.nodeRankedSignatureIndexes = nodeRankedSignatureIndexes;
                break;
        }
        
        dataSet.nodes = nodes;
        dataSet.rootNodes = rootNodes;
        dataSet.profileOffsets = profileOffsets;

        strings.read(reader);
        components.read(reader);
        maps.read(reader);
        properties.read(reader);
        values.read(reader);
        profiles.read(reader);
        signatures.read(reader);
        
        switch(dataSet.versionEnum) {
            case PatternV32:
                signatureNodeOffsets.read(reader);
                nodeRankedSignatureIndexes.read(reader);
                break;
        }
        
        rankedSignatureIndexes.read(reader);
        nodes.read(reader);
        rootNodes.read(reader);
        profileOffsets.read(reader);
  
        if (init) {
            // Set references between objects.
            dataSet.init();

            // The following lists will not be needed anymore
            // so they can be freed.
            dataSet.signatureNodeOffsets = null;
            dataSet.nodeRankedSignatureIndexes = null;
            
            // Request garbage collection as a lot of memory has been freed.
            System.gc();
        }
    }
}
