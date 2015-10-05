package fiftyone.mobile.detection.readers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;

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
 * Due to the size of the Trie tree structure, the data cannot fit into a single
 * ByteBuffer. Therefore, this class is used to wrap the file channel and and
 * become logically one channel.
 */
public class TrieReader {

    /**
     * Buffers mapped to the files channel.
     */
    private ArrayList<MappedByteBuffer> _buffers;
    /**
     * The File Channel linked to the trie data.
     */
    private final FileChannel _triFile;
    /**
     * The start position of the tree in the trie file.
     */
    private final long _start;
    /**
     * Logical position of the data needed in the file.
     */
    private long _position;
    /**
     * The index of the buffer that _position is pointed at.
     */
    private int _bufferIndex;
    private long _globalPosition;

    /**
     *
     * Creates the class by creating multiple ByteBuffers that can be read from.
     *
     * @param reader File channel to read from.
     * @throws java.io.IOException indicates an I/O exception occurred
     */
    public TrieReader(final FileChannel reader) throws IOException {
        _position = 0;
        _globalPosition = 0;
        _bufferIndex = 0;
        _start = reader.position();
        long readPosition = _start;
        _triFile = reader;
        double buffersNeededDouble = (double) reader.size() / (double) Integer.MAX_VALUE;
        int buffersNeeded = (int) Math.ceil(buffersNeededDouble);
        // Allocate the exact amount of buffers to ArrayList
        _buffers = new ArrayList<MappedByteBuffer>(buffersNeeded);
        while (readPosition != reader.size()) {
            long bytesLeft = reader.size() - readPosition;
            int bytesToRead = 0;
            if (bytesLeft > Integer.MAX_VALUE) {
                bytesToRead = Integer.MAX_VALUE;
            } else {
                bytesToRead = (int) bytesLeft;
            }
            MappedByteBuffer buffer = reader.map(MapMode.READ_ONLY,
                    readPosition,
                    bytesToRead);

            readPosition += bytesToRead;
            _buffers.add(buffer);
        }
    }

    /**
     *
     * Returns the current logical position value.
     *
     * @return the current logical position in the file.
     */
    public long getPos() {
        return _globalPosition;// + _start;
    }

    /**
     *
     * Sets the logical position in the file.
     *
     **** @param position position value.
     */
    public void setPos(long position) {
        //position -= _start;
        _globalPosition = position;
        for (int i = 0; i < _buffers.size(); i++) {
            if (position > _buffers.get(i).limit()) {
                position -= _buffers.get(i).limit();
            } else {
                int p = (int) position;
                _bufferIndex = i;
                _buffers.get(i).position(p);
                break;
            }
        }
        _position = position;
    }

    /**
     *
     * Read an Unsigned byte from the file at the current location.
     *
     * @return The value read.
     */
    public short readUByte() {
        short s = 0;

        byte[] value = getValue(TrieReader.DataType.Byte);
        s |= (value[0] & 0xFF);

        return s;
    }

    /**
     *
     * Read an Unsigned byte from the file at a specified location.
     *
     **** @param position Position to read from.
     * @return The value read.
     */
    public short readUByte(long position) {
        setPos(position);
        return readUByte();
    }

    /**
     * Read an Unsigned short from the file at the current location.
     *
     * @return The value read.
     */
    public int readUShort() {
        int i = 0;

        byte[] value = getValue(TrieReader.DataType.Unsigned_Short);
        i |= (value[1] & 0xFF);
        i <<= 8;
        i |= (value[0] & 0xFF);

        return i;
    }

    /**
     *
     * Read an Unsigned short from the file at a specified location.
     *
     **** @param position Position to read from.
     * @return The value read.
     */
    public int readUShort(long position) {
        setPos(position);
        return readUShort();
    }

    /**
     *
     * Read an Unsigned integer from the file at the current location.
     *
     * @return The Value read.
     * @throws IOException indicates an I/O exception occurred
     */
    public long readUInt() throws IOException {
        long l = 0;

        byte[] value = getValue(TrieReader.DataType.Unsigned_Int);
        l |= (value[3] & 0xFF);
        l <<= 8;
        l |= (value[2] & 0xFF);
        l <<= 8;
        l |= (value[1] & 0xFF);
        l <<= 8;
        l |= (value[0] & 0xFF);

        if (l > (long) 2 * Integer.MAX_VALUE) {
            throw new IOException("readInt: value is greater then unsigned integer.");
        }

        return l;
    }

    /**
     *
     * Read an signed integer from the file at the current location.
     *
     * @return The Value read.
     * @throws IOException indicates an I/O exception occurred
     */
    public int readInt() throws IOException {
        int r = 0;

        byte[] value = getValue(TrieReader.DataType.Unsigned_Int);
        for (int i = 0; i < value.length; i++) {
            r += ((int) value[i] & 0xffL) << (8 * i);
        }
        /*
         i |= (value[3] & 0xFF);
         i <<= 8;
         i |= (value[2] & 0xFF);
         i <<= 8;
         i |= (value[1] & 0xFF);
         i <<= 8;
         i |= (value[0] & 0xFF);*/

        return r;
    }

    /**
     *
     * Read an signed long from the file at the current location.
     *
     * @return The Value read.
     */
    public long readLong() {
        long l = 0;
        byte[] value = getValue(TrieReader.DataType.Long);

        for (int i = 0; i < value.length; i++) {
            l += ((long) value[i] & 0xffL) << (8 * i);
        }

        return l;
    }

    /**
     *
     * Read an signed long from the file at a specified location.
     *
     **** @param position Position to read from.
     * @return The value read.
     * @throws IOException indicates an I/O exception occurred
     */
    public long readLong(long position) throws IOException {
        setPos(position);
        return readLong();
    }

    /**
     * Gets the MappedByteBuffer for that this class is currently positioned on,
     * automatically incrementing position and changing buffer if required.
     */
    private byte getNextByte() {
        int pos = (int) _position;
        _globalPosition++;
        byte returnByte = _buffers.get(_bufferIndex).get(pos);
        if (_buffers.get(_bufferIndex).remaining() == 0) {
            _bufferIndex++;
            _position = 0;
        } else {
            _position++;
        }
        return returnByte;
    }

    public byte readByte() {
        return getNextByte();
    }

    public byte[] readBytes(final int length) {
        final byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = this.getNextByte();
        }
        return bytes;
    }

    /**
     *
     * Reads a certain data type from the file e.g. byte, Unsigned Integer etc.
     *
     **** @param dataType Type of data to read.
     * @return byte array of data.
     */
    private byte[] getValue(TrieReader.DataType dataType) {
        // Array to hold data that is read.
        byte[] value;

        // Amount of data to read.
        int readSize;

        // Determine size of read
        switch (dataType) {
            case Byte:
                value = new byte[1];
                readSize = 1;
                break;
            case Unsigned_Short:
                value = new byte[2];
                readSize = 2;
                break;
            case Unsigned_Int:
                value = new byte[4];
                readSize = 4;
                break;
            case Long:
                value = new byte[8];
                readSize = 8;
                break;
            default:
                return null;
        }

        for (int i = 0; i < readSize; i++) {
            value[i] = getNextByte();
        }
        return value;
    }

    /**
     * Clears the ByteBuffers and closes the File Channel.
     *
     * @throws IOException indicates an I/O exception occurred
     */
    public void close() throws IOException {
        for (ByteBuffer b : _buffers) {
            b.clear();
            b = null;
        }
        _triFile.close();
    }

    /**
     * Defines what data type is being read from a given buffer.
     */
    private static enum DataType {

        Byte, Unsigned_Short, Unsigned_Int, Long;
    }
}
