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
 * This Source Code Form is ?Incompatible With Secondary Licenses?, as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */
package fiftyone.mobile.detection.readers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Encapsulates either a file containing the uncompressed data structures 
 * used by the data set.
 */
public class SourceFile extends SourceFileBase {
    
    /**
     * File input stream connected to the underlying source file.
     */
    private final FileInputStream fileInputStream;
    
    /**
     * File channel connected to the file input stream.
     */
    private final FileChannel channel;
    
    /**
     * Creates the source from the file provided.
     * @param fileName File source of the data.
     * @param isTempFile True if the file should be deleted when the source 
     * is disposed.
     * @throws java.io.FileNotFoundException
     */
    public SourceFile(String fileName, boolean isTempFile) 
            throws FileNotFoundException {
        super(fileName, isTempFile);
        fileInputStream = new FileInputStream(fileName);
        channel = fileInputStream.getChannel();
    }

    /**
     * Creates a new ByteBuffer from the file located on the hard drive.
     * @return ByteBuffer ready to read data from the data file on hard drive.
     * @throws java.io.IOException
     */
    @Override
    ByteBuffer createStream() throws IOException {
        MappedByteBuffer byteBuffer = channel.map(
                FileChannel.MapMode.READ_ONLY,
                0,
                channel.size());
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return byteBuffer;
    }
    
    /**
     * Close any file references, release resources and then try to 
     * delete the underlying file if 
     * @throws java.io.IOException
     */
    @Override
    public void close() throws IOException {
        // Close the input stream and therefore the channel.
        fileInputStream.close();
        
        // Java does not provide a method to explicitly unmap the buffer from 
        // the underlying file. The implementation of memory mapped files varies
        // across operating systems. As such there is no reliable way of 
        // guaranteeing the lock on the underlying file is released. For this
        // reason System.gc() is called when the SourceFile class is closed
        // to attempt to free the underlying file. The alternative would be to
        // not use memory mapped files which removes a performance advantage.
        System.gc();
        
        // Delete the file if it's temporary.
        super.deleteFile();
    }
}