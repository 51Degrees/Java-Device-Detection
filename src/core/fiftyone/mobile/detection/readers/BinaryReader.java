package fiftyone.mobile.detection.readers;

import fiftyone.mobile.detection.Disposable;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;

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
public class BinaryReader implements Disposable {

    /**
     * List of integers used to create arrays of integers where the length of
     * the list is not known before reading starts.
     */
    public final List<Integer> list = new ArrayList<Integer>();
    private ByteBuffer byteBuffer;
    private FileChannel channel;

    public BinaryReader(byte[] data) {
        byteBuffer = ByteBuffer.wrap(data);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public BinaryReader(FileInputStream fileInputStream) throws IOException {
        channel = fileInputStream.getChannel();
        byteBuffer = channel.map(
                MapMode.READ_ONLY,
                0,
                channel.size());
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public BinaryReader(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
        this.byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public void setPos(int pos) {
        byteBuffer.position(pos);
    }

    public byte readByte() {
        return byteBuffer.get();
    }

    public short readInt16() {
        return byteBuffer.getShort();
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
    
    @Override
    public void dispose()
    {
        if(channel != null)
        {
            try
            {
                channel.close();
            }
            catch (IOException ex)
            {
            }
        }
        byteBuffer = null;
    }
}
