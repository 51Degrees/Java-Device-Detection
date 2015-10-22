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
package fiftyone.mobile.detection.entities.stream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Encapsulates either a file containing the uncompressed data structures 
 * used by the data set.
 */
class SourceFile extends SourceFileBase {
    
    /**
     * Encapsulates all the instances that need to be tracked for a mapped
     * file byte buffer.
     */
    private class FileHandle {
        final FileInputStream fileInputStream;
        
        /**
         * A memory mapped buffer to the underlying file. 
         * 
         * Java does not provide a method to explicitly unmap the buffer from 
         * the underlying file. The implementation of memory mapped files varies
         * across operating systems. As such there is no reliable way of 
         * guaranteeing the lock on the underlying file is released. For this
         * reason System.gc() is called when the SourceFile class is closed
         * to attempt to free the underlying file. The alternative would be to
         * not use memory mapped files which removes a performance advantage.
         */
        MappedByteBuffer byteBuffer;
        
        /**
         * Constructs a new instance of FileHandle connected to the file
         * provided.
         * @param file to create the handle from
         * @throws FileNotFoundException
         * @throws IOException 
         */
        FileHandle(File file) throws FileNotFoundException, IOException {
            fileInputStream = new FileInputStream(file);
            FileChannel channel = fileInputStream.getChannel();
            byteBuffer = channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    0,
                    channel.size());
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }

        /**
         * Closes the underlying resources.
         * @throws IOException 
         */
        private void close() throws IOException {
            fileInputStream.close();
        }
    }
    
    /**
     * List of file handles creates by the class. Used to ensure they're all
     * disposed of correctly.
     */
    private final Queue<FileHandle> handles = new LinkedList<FileHandle>();
    
    /**
     * Creates the source from the file provided.
     * @param fileName File source of the data.
     * @param isTempFile True if the file should be deleted when the source 
     * is disposed.
     */
    SourceFile(String fileName, boolean isTempFile) {
        super(fileName, isTempFile);
    }

    /**
     * Creates a new ByteBuffer from the file located on the hard drive.
     * @return ByteBuffer ready to read data from the data file on hard drive.
     * @throws java.io.IOException
     */
    @Override
    ByteBuffer createStream() throws IOException {
        FileHandle handle = new FileHandle(super.getFile());
        synchronized(handles) {
            handles.add(handle);
        }
        return handle.byteBuffer;
    }
    
    /**
     * Close any file references, release resources and then try to 
     * delete the underlying file if 
     * @throws java.io.IOException
     */
    @Override
    public void close() throws IOException {
        // Dispose of all the file handles.
        synchronized(handles) {
            for(FileHandle handle : handles) {
                handle.close();
            }
            // See docs for FileHandle.byteBuffer for explanation as to why
            // garbage collector is called explicitly.
            System.gc();
        }
        
        // Delete the file if it's temporary.
        super.deleteFile();
    }
}