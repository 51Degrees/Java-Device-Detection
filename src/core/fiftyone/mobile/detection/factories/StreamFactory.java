package fiftyone.mobile.detection.factories;

import fiftyone.mobile.detection.Dataset;
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
import fiftyone.mobile.detection.entities.stream.Source;
import fiftyone.mobile.detection.entities.stream.StreamFixedList;
import fiftyone.mobile.detection.entities.stream.StreamVariableList;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.FileInputStream;
import java.io.IOException;

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
 * will be initialised very quickly as only the header information is read.
 * Entities are then created when requested by the detection process and stored
 * in a cache to avoid being recreated if their requested again after a short
 * period of time. <p> The very small data structures RootNodes, Properties and
 * Components are always /stored in memory as there is no benefit retrieving
 * them every time they're needed. <p> For more information see
 * http://51degrees.mobi/Support/Documentation/Java
 */
public final class StreamFactory {

    public static Dataset create(byte[] data) throws IOException {
        return read(
                new BinaryReader(data),
                new Source(data));
    }

    public static Dataset create(String filename) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(filename);
        try {
            return read(
                    new BinaryReader(fileInputStream),
                    new Source(filename));
        } catch (Exception e) {
            return null;
        } finally {
            fileInputStream.close();
        }
    }

    static Dataset read(BinaryReader reader, Source source) throws IOException {
        Dataset dataSet = new Dataset(reader);
        
        dataSet.strings = new StreamVariableList<AsciiString>(dataSet, reader,
                source, new AsciiStringFactory());
        MemoryFixedList<Component> components = new MemoryFixedList<Component>(
                dataSet, reader, new ComponentFactory());
        dataSet.components = components;
        MemoryFixedList<Map> maps = new MemoryFixedList<Map>(
                dataSet, reader, new MapFactory());
        dataSet.maps = maps;
        MemoryFixedList<Property> properties = new MemoryFixedList<Property>(
                dataSet, reader, new PropertyFactory());
        dataSet.properties = properties;
        dataSet.values = new StreamFixedList<Value>(dataSet, reader, source,
                new ValueFactory());
        dataSet.profiles = new StreamVariableList<Profile>(dataSet, reader,
                source, new ProfileFactory());
        dataSet.signatures = new StreamFixedList<Signature>(dataSet, reader,
                source, new SignatureFactory(dataSet));
        dataSet.rankedSignatureIndexes = new StreamFixedList<RankedSignatureIndex>(
                dataSet, reader, source, new RankedSignatureIndexFactory());
        dataSet.nodes = new StreamVariableList<Node>(dataSet, reader, source,
                new NodeFactory());
        MemoryFixedList<Node> rootNodes = new MemoryFixedList<Node>(dataSet,
                reader, new RootNodeFactory());
        dataSet.rootNodes = rootNodes;
        dataSet.profileOffsets = new StreamFixedList<ProfileOffset>(
                dataSet, reader, source, new ProfileOffsetFactory());
        
        // Read into memory all the small lists which are frequently accessed.
        reader.setPos(components.header.getStartPosition());
        components.read(reader);
        reader.setPos(maps.header.getStartPosition());
        maps.read(reader);
        reader.setPos(properties.header.getStartPosition());
        properties.read(reader);
        reader.setPos(rootNodes.header.getStartPosition());
        rootNodes.read(reader);

        return dataSet;
    }
}
