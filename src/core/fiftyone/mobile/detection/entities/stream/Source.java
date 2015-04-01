package fiftyone.mobile.detection.entities.stream;

import fiftyone.mobile.detection.Disposable;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

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
 * Encapsulates either a byte array or a file containing the uncompressed data
 * structures used by the data set. <p> Must be disposed to ensure that the
 * readers are closed and the file free for other uses. Does not need to be
 * disposed if a byte array is used.
 */
public class Source implements Disposable {

    private final FileInputStream fileInputStream;
    private FileChannel channel;
    private final byte[] data;

    public Source(String filename) throws FileNotFoundException {
        fileInputStream = new FileInputStream(filename);
        this.data = null;
    }

    public Source(byte[] data) {
        this.data = data;
        this.fileInputStream = null;
    }

    @Override
    public void dispose() {
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (IOException ex) {
                // Nothing we can do at this point. Ignore.
            }
        }
        if(channel != null) {
            try {
               channel.close();
            }
         catch(IOException ex){}
        }
    }

    /**
     * Creates a new reader and stores a reference to it.
     *
     * @return A reader open for read access to the stream
     * @throws java.io.IOException indicates an I/O exception occurred
     */
    public synchronized BinaryReader createReader() throws IOException {
        if (data != null) {
            return new BinaryReader(createByteBuffer());
        }
        return new BinaryReader(createMappedByteBuffer());
    }

    private ByteBuffer createByteBuffer() throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return byteBuffer;
    }

    private MappedByteBuffer createMappedByteBuffer() throws IOException {
        channel = fileInputStream.getChannel();
        MappedByteBuffer mappedBuffer = channel.map(
                MapMode.READ_ONLY,
                0,
                channel.size());
        mappedBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return mappedBuffer;
    }
}
