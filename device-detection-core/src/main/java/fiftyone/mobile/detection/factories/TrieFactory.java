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

import fiftyone.mobile.detection.TrieProvider;
import fiftyone.mobile.detection.TrieProviderV3;
import fiftyone.mobile.detection.TrieProviderV32;
import fiftyone.mobile.detection.entities.stream.TriePool;
import fiftyone.mobile.detection.entities.stream.TrieSource;
import fiftyone.mobile.detection.readers.TrieReader;
import java.io.*;

/**
 * Reader used to create a provider from data structured in a decision tree
 * format.
 */
public class TrieFactory {

    /**
     * Creates a new provider from the supplied pool of readers.
     *
     * @param pool Pool of readers to use.
     * @return A new provider initialised using the pool of readers.
     * @throws IOException indicates an I/O exception occurred
     */
    public static TrieProvider create(TriePool pool) throws IOException {
        TrieReader reader = pool.getReader();
        
        try {
            int version = reader.readUShort();
            switch (version) {
                case 3:
                    return new TrieProviderV3(
                        new String(reader.readBytes((int) reader.readUInt())), 
                        readStrings(reader), 
                        readProperties(reader), 
                        readDevices(reader), 
                        readLookupList(reader), 
                        reader.readLong(), 
                        reader.getPos(), 
                        pool);
                case 32:
                    return new TrieProviderV32(
                        new String(reader.readBytes((int) reader.readUInt())), 
                        readStrings(reader), 
                        readHeaders(reader), 
                        readProperties(reader), 
                        readDevices(reader), 
                        readLookupList(reader), 
                        reader.readLong(), 
                        reader.getPos(), 
                        pool);
                default:
                    throw new IllegalArgumentException("The file you are "
                            + "trying to use is either of the wrong format or "
                            + "compressed or is not supported by this version "
                            + "of the API.");
            }
        } finally {
            pool.release(reader);
        }
    }
    
    /**
     * Creates a new provider from the binary file supplied.
     * @param file Binary file to use to create the provider.
     * @return A new provider initialised with data from the file provided.
     * @throws java.io.IOException
     */
    public static TrieProvider create(String file) throws IOException {
        return create(file, false);
    }
    
    public static TrieProvider create(String file, boolean isTempFile) throws IOException {
        File f = new File(file);
        if (f.exists() && f.isFile()) {
            return create(new TriePool(new TrieSource(file, isTempFile)));
        }
        throw new IOException("Selected filename is either a directory or does not exist: " + file);
    }

    private static short[] readLookupList(TrieReader reader) throws IOException {
        short[] lookupList = new short[reader.readInt()];
        for (int i = 0; i < lookupList.length; i++) {
            lookupList[i] = reader.readUByte();
        }
        return lookupList;
    }

    private static byte[] readStrings(TrieReader reader) throws IOException {
        return reader.readBytes(reader.readInt());
    }

    private static byte[] readProperties(TrieReader reader) throws IOException {
        return reader.readBytes(reader.readInt());
    }

    private static byte[] readDevices(TrieReader reader) throws IOException {
        return reader.readBytes(reader.readInt());
    }

    private static byte[] readHeaders(TrieReader reader) throws IOException {
        return reader.readBytes((int)reader.readUInt());
    }
}
