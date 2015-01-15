package fiftyone.mobile.detection.factories;

import fiftyone.mobile.detection.TrieProvider;
import fiftyone.mobile.detection.readers.TrieReader;
import java.io.*;

/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
 * Reader used to create a provider from data structured in a decision tree
 * format.
 */
public class TrieFactory {

    /**
     * Creates a new provider from the binary file supplied.
     *
     * @param fileName Binary file to use to create the provider.
     * @return A new provider initialised with data from the file provided.
     * @throws IOException indicates an I/O exception occurred
     */
    public static TrieProvider create(String fileName) throws IOException {
        File file = new File(fileName);
        if (file.exists()) {
            FileInputStream stream = new FileInputStream(file);

            TrieReader reader = new TrieReader(stream.getChannel());

            // Check the version number is correct for this API.
            //Version version = new Version(reader.readInt(), reader.readInt(),
            //		reader.readInt(), reader.readInt());

            int version = reader.readUShort();

            // Add this
                /*if (version != BinaryConstants.FormatVersion.Major)
             {
             throw new MobileException(String.Format(
             "Version mismatch. Data is version '{0}' for '{1}' reader",
             version,
             BinaryConstants.FormatVersion.Major));
             }*/

            // Create the new provider.
            return new TrieProvider(
                    new String(reader.readBytes((int) reader.readUInt())),
                    ReadStrings(reader),
                    ReadProperties(reader),
                    ReadDevices(reader),
                    ReadLookupList(reader),
                    reader.readLong(),
                    reader.getPos(),
                    fileName);

        } else {
            return null;
        }
    }

    private static short[] ReadLookupList(TrieReader reader) throws IOException {
        short[] lookupList = new short[reader.readInt()];
        for (int i = 0; i < lookupList.length; i++) {
            lookupList[i] = reader.readUByte();
        }
        return lookupList;
    }

    private static byte[] ReadStrings(TrieReader reader) throws IOException {
        return reader.readBytes(reader.readInt());
    }

    private static byte[] ReadProperties(TrieReader reader) throws IOException {
        return reader.readBytes(reader.readInt());
    }

    private static byte[] ReadDevices(TrieReader reader) throws IOException {
        return reader.readBytes(reader.readInt());
    }
}
