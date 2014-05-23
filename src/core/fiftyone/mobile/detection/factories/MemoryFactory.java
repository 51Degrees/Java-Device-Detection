package fiftyone.mobile.detection.factories;

import java.io.FileInputStream;
import java.io.IOException;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.entities.AsciiString;
import fiftyone.mobile.detection.entities.Component;
import fiftyone.mobile.detection.entities.Map;
import fiftyone.mobile.detection.entities.Node;
import fiftyone.mobile.detection.entities.Profile;
import fiftyone.mobile.detection.entities.ProfileOffset;
import fiftyone.mobile.detection.entities.RankedSignatureIndex;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.entities.Signature;
import fiftyone.mobile.detection.entities.Value;
import fiftyone.mobile.detection.entities.memory.MemoryFixedList;
import fiftyone.mobile.detection.entities.memory.MemoryVariableList;
import fiftyone.mobile.detection.readers.BinaryReader;

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
 * All the entities are held in memory and the source data structure not
 * referenced once the data set is created. <p> The memory usage of the
 * resulting data set following initialisation will be consistent. The
 * performance of the data set will be very fast compared to the stream based
 * implementation as all required data is loaded into memory and references
 * between related objects set at initialisation. However overall memory usage
 * will be higher than the stream based implementation on lightly loaded
 * environments. <p> Initialisation may take several seconds depending on system
 * performance. Initialisation calculates all the references between entities.
 * If initialisation is not performed then references will be calculated when
 * needed. As such avoiding initialisation improves the time taken to create the
 * data set, at the expense of performance for the initial detections. The
 * default setting is to initialise the data set. <p> For more information see
 * http://51degrees.mobi/Support/Documentation/Java
 */
public final class MemoryFactory {

    public static Dataset create(byte[] data) throws IOException {
        return create(data, false);
    }

    public static Dataset create(byte[] data, boolean init) throws IOException {
        return read(new BinaryReader(data), init);
    }

    public static Dataset create(String filename) throws IOException {
        return create(filename, false);
    }

    public static Dataset create(String filename, boolean init) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(filename);
        try {
            return read(new BinaryReader(fileInputStream), init);
        } finally {
            fileInputStream.close();
        }
    }

    /*
     * Creates a new DetectorDataSet from the binary reader provided. A
     * DetectorDataSet is constructed using the reader to retrieve. the header
     * information. This is then passed to the Read methods to create the lists
     * before reading the data into memory. Finally it initialise is required
     * references between entities are worked out and stored.
     * 
     * @param reader
     *            Reader connected to the source data structure and positioned
     *            to start reading.
     * @param init
     *            True to indicate that the data set should be fully initialised
     * @return A DetectorDataSet filled with data from the reader
     */
    public static Dataset read(BinaryReader reader, boolean init) throws IOException {
        Dataset dataSet = new Dataset(reader);

        MemoryVariableList<AsciiString> strings = new MemoryVariableList<AsciiString>(
                dataSet, reader, new AsciiStringFactory());
        MemoryFixedList<Component> components = new MemoryFixedList<Component>(
                dataSet, reader, new ComponentFactory());
        MemoryFixedList<Map> maps = new MemoryFixedList<Map>(
                dataSet, reader, new MapFactory());
        MemoryFixedList<Property> properties = new MemoryFixedList<Property>(
                dataSet, reader, new PropertyFactory());
        MemoryFixedList<Value> values = new MemoryFixedList<Value>(dataSet,
                reader, new ValueFactory());
        MemoryVariableList<Profile> profiles = new MemoryVariableList<Profile>(
                dataSet, reader, new ProfileFactory());
        MemoryFixedList<Signature> signatures = new MemoryFixedList<Signature>(
                dataSet, reader, new SignatureFactory(dataSet));
        MemoryFixedList<RankedSignatureIndex> rankedSignatureIndexes =
                new MemoryFixedList<RankedSignatureIndex>(
                dataSet, reader, new RankedSignatureIndexFactory());
        MemoryVariableList<Node> nodes = new MemoryVariableList<Node>(dataSet,
                reader, new NodeFactory());
        MemoryFixedList<Node> rootNodes = new MemoryFixedList<Node>(dataSet,
                reader, new RootNodeFactory());
        MemoryFixedList<ProfileOffset> profileOffsets = new MemoryFixedList<ProfileOffset>(
                dataSet, reader, new ProfileOffsetFactory());

        dataSet.strings = strings;
        dataSet.components = components;
        dataSet.maps = maps;
        dataSet.properties = properties;
        dataSet.values = values;
        dataSet.profiles = profiles;
        dataSet.signatures = signatures;
        dataSet.rankedSignatureIndexes = rankedSignatureIndexes;
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
        rankedSignatureIndexes.read(reader);
        nodes.read(reader);
        rootNodes.read(reader);
        profileOffsets.read(reader);

        if (init) {
            // Set references between objects.
            dataSet.init();

            // request garbage collection as a lot of memory has been freed.
            System.gc();
        }

        return dataSet;
    }
}
