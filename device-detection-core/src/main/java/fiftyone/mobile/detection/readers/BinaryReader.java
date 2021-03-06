/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
 * Caversham, Reading, Berkshire, United Kingdom RG4 7BY
 * 
 * This Source Code Form is the subject of the following patents and patent
 * applications, owned by 51Degrees Mobile Experts Limited of 5 Charlotte
 * Close, Caversham, Reading, Berkshire, United Kingdom RG4 7BY: 
 * European Patent No. 2871816;
 * European Patent Application No. 17184134.9;
 * United States Patent Nos. 9,332,086 and 9,350,823; and
 * United States Patent Application No. 15/686,066.
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

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a way for the API to read from the data file and to retrieve 
 * entities of various type.
 * <p>
 * Objects of this class should not be created directly as they are part of the 
 * internal logic.
 */
public class BinaryReader implements Closeable {

    /**
     * List of integers used to create arrays of integers where the length of
     * the list is not known before reading starts.
     */
    public final List<Integer> list = new ArrayList<Integer>();
    private ByteBuffer byteBuffer;
    private FileChannel channel;

    /**
     * Creates a new BinaryReader object from byte array.
     * 
     * @param data byte array to use as the source.
     */
    public BinaryReader(byte[] data) {
        byteBuffer = ByteBuffer.wrap(data);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }
    
    /**
     * Creates a new BinaryReader object from file input stream.
     * 
     * @param fileInputStream an open stream to the data file.
     * @throws IOException if there was a problem accessing data file.
     */
    public BinaryReader(FileInputStream fileInputStream) throws IOException {
        channel = fileInputStream.getChannel();
        byteBuffer = channel.map(
                MapMode.READ_ONLY,
                0,
                channel.size());
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Creates a new BinaryReader from a byte buffer.
     * 
     * @param byteBuffer mapped to the data file.
     */
    public BinaryReader(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
        this.byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Sets position.
     * 
     * @param pos position to set.
     */
    public void setPos(int pos) {
        byteBuffer.position(pos);
    }
    
    /**
     * @return current position in the byte buffer.
     */
    public int getPos() {
        return byteBuffer.position();
    }

    public byte readByte() {
        return byteBuffer.get();
    }

    public short readInt16() {
        return byteBuffer.getShort();
    }
    
    public int readUInt16() {
        short s = byteBuffer.getShort();
        int intVal = s >= 0 ? s : 0x10000 + s; 
        return intVal;
    }

    public int readInt32() {
        return byteBuffer.getInt();
    }

    public boolean readBoolean() {
        return byteBuffer.get() != 0;
    }

    public byte[] readBytes(final int length) {
        byte[] bytes = new byte[length];
        byteBuffer.get(bytes);
        return bytes;
    }
    
    /**
     * Set the byte buffer to null to prevent any further access to the under
     * lying data. This should be done before the channel is closed as the 
     * byte buffer could be tied to the channel. Any subsequent access to the 
     * methods will fail with a null object exception.
     */
    @Override
    public void close() {
        byteBuffer = null;
        if (channel != null) {
            try {
                channel.close();
            }
            catch (IOException ex)
            {
                // Do nothing.
            }
        }
    }
}
