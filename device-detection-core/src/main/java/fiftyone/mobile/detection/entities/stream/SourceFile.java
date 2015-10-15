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
package fiftyone.mobile.detection.entities.stream;

import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
/**
 * Encapsulates either a file containing the uncompressed data structures 
 * used by the data set.
 */
public class SourceFile extends SourceFileBase {
    
    /**
     * Encapsulates all the instances that need to be tracked for a mapped
     * file byte buffer.
     */
    private class FileHandle {
        final FileInputStream fileInputStream;
        final FileChannel channel;
        final ByteBuffer byteBuffer;
        
        /**
         * Constructs a new instance of FileHandle connected to the file
         * provided.
         * @param file to create the handle from
         * @throws FileNotFoundException
         * @throws IOException 
         */
        FileHandle(File file) throws FileNotFoundException, IOException {
            fileInputStream = new FileInputStream(file);
            channel = fileInputStream.getChannel();
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
        private void dispose() throws IOException {
            channel.close();
            fileInputStream.close();
        }
    }
    
    /**
     * List of file handles creates by the class. Used to ensure they're all
     * disposed of correctly.
     */
    private final List<FileHandle> handles = new ArrayList<FileHandle>();
    
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
        ByteBuffer byteBuffer = null;
        try {
            FileHandle handle = new FileHandle(super.getFile());
            handles.add(handle);
            byteBuffer = handle.byteBuffer;
        } catch (IOException ex) {
            //TODO: handle exception.
        }
        return byteBuffer;
    }
    
    /**
     * Close any file references, release resources and then try to 
     * delete the file.
     */
    @Override
    public void close() {
        // Dispose of all the binary readers created from the byte buffers.
        super.close();
        // Dispose of all the file hanldes.
        for(FileHandle handle : handles) {
            try {
                handle.dispose();
            } catch (IOException ex) {
                //TODO: handle exception.
            }
        }
        // Delete the file if it's temporary.
        super.deleteFile();
    }
}
