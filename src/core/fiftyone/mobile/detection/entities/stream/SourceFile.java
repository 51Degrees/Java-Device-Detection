package fiftyone.mobile.detection.entities.stream;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 * This Source Code Form is ?Incompatible With Secondary Licenses?, as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */
/**
 * Encapsulates either a file containing the uncompressed data structures 
 * used by the data set.
 */
public class SourceFile extends SourceFileBase {
    /**
     * Used for reading from the data file after input stream closed.
     */
    private ByteBuffer byteBuffer;
    /**
     * Input bytes from a file in a file system.
     */
    private FileInputStream fileInputStream;
    /**
     * A channel for reading, writing, mapping, and manipulating a file. 
     */
    private FileChannel channel;
    
    /**
     * Creates the source from the file provided.
     * @param fileName File source of the data.
     * @param isTempFile True if the file should be deleted when the source 
     * is disposed.
     */
    public SourceFile(String fileName, boolean isTempFile) {
        super(fileName, isTempFile);
    }

    /**
     * Creates a new ByteBuffer from the file located on the hard drive.
     * @return ByteBuffer ready to read data from the data file on hard drive.
     */
    @Override
    public ByteBuffer createStream() {
        byteBuffer = null;
        fileInputStream = null;
        try {
            //Open input stream from file.
            fileInputStream = new FileInputStream(getFile());
            
            channel = fileInputStream.getChannel();
            byteBuffer = channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    0,
                    channel.size());
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            
            //Close the input stream.
            if (fileInputStream != null)
                fileInputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(SourceFile.class.getName())
                                       .log(Level.SEVERE, null, ex);
        }
        return byteBuffer;
    }
    
    /**
     * Close any file references, release resources and then try to 
     * delete the file.
     */
    @Override
    public void dispose() {
        super.dispose();
        super.deleteFile();
        try {
            if (byteBuffer != null)
                byteBuffer.clear();
            if (fileInputStream != null)
                fileInputStream.close();
            if (channel.isOpen())
                channel.close();
        } catch (IOException ex) {
            Logger.getLogger(SourceFile.class.getName())
                                             .log(Level.SEVERE, null, ex);
        }
    }
}
