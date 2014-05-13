package fiftyone.mobile.detection.trie;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Due to the size of the Trie tree structure, the data cannot fit into a single
 * ByteBuffer. Therefore, this class is used to wrap the file channel and and
 * become logically one channel.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class FiftyOneMapper {

    /**
     * Creates a logger for this class
     */
    private static final Logger _logger = LoggerFactory.getLogger(FiftyOneMapper.class);
    
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
    /**
     * The position with the buffer that _position is pointed at.
     */
    private int _relativePosition;

    /**
     *
     * Creates the class by creating multiple ByteBuffers that can be read from.
     *
     * @param reader File channel to read from.
     */
    public FiftyOneMapper(final FileChannel reader) throws IOException {
        _position = 0;
        _bufferIndex = 0;
        _relativePosition = 0;
        _start = reader.position();
        long readPosition = _start;
        _triFile = reader;
        // Allocate the exact amount of buffers to ArrayList
        _buffers = new ArrayList<MappedByteBuffer>(3);
        while(readPosition != reader.size()) {
            long size = reader.size();
            long bytesLeft = reader.size() - readPosition;
            int bytesToRead = 0;
            if(bytesLeft > Integer.MAX_VALUE) {
                bytesToRead = Integer.MAX_VALUE;
            }
            else {
                bytesToRead = (int)bytesLeft;
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
    public long getPosition() {
        return _position + _start;
    }

    /**
     *
     * Sets the logical position in the file.
     *
     * @param position position value.
     */
    public void setPosition(long position) {
        position -= _start;
        for (int i = 0; i < _buffers.size(); i++) 
        {
            if (position > _buffers.get(i).limit()) 
            {
                position -= _buffers.get(i).limit();
            } 
            else 
            {
                _bufferIndex = i;
                _buffers.get(i).position((int) position);
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
    public short readByte() {
        short s = 0;

        byte[] value = getValue(DataType.Byte);
        s |= (value[0] & 0xFF);

        return s;
    }

    /**
     *
     * Read an Unsigned byte from the file at a specified location.
     *
     * @param position Position to read from.
     * @return The value read.
     */
    public short readByte(long position) {
        setPosition(position);
        return readByte();
    }

    /**
     * Read an Unsigned short from the file at the current location.
     *
     * @return The value read.
     */
    public int readShort() {
        int i = 0;

        byte[] value = getValue(DataType.Unsigned_Short);
        i |= (value[1] & 0xFF);
        i <<= 8;
        i |= (value[0] & 0xFF);

        return i;
    }

    /**
     *
     * Read an Unsigned short from the file at a specified location.
     *
     * @param position Position to read from.
     * @return The value read.
     */
    public int readShort(long position) {
        setPosition(position);
        return readShort();
    }
    
    /**
     *
     * Read an Unsigned integer from the file at the current location.
     *
     * @return The Value read.
     */
    public long readInt() {
        long l = 0;

        byte[] value = getValue(DataType.Unsigned_Int);
        l |= (value[3] & 0xFF);
        l <<= 8;
        l |= (value[2] & 0xFF);
        l <<= 8;
        l |= (value[1] & 0xFF);
        l <<= 8;
        l |= (value[0] & 0xFF);
        
        if (l > (long) 2 * Integer.MAX_VALUE) {
            _logger.error("readInt: value is greater then unsigned integer.");
        }

        return l;
    }

    /**
     *
     * Read an Unsigned integer from the file at a specified location.
     *
     * @param position Position to read from.
     * @return The value read.
     */
    public long readInt(long position) {
        setPosition(position);
        return readInt();
    }
    
    /**
     * Gets the MappedByteBuffer for that this class is currently positioned on,
     * automatically incrementing position and changing buffer if required.
     */
    private byte getNextByte() {
        int pos = (int)_position;
        byte returnByte = _buffers.get(_bufferIndex).get(pos);
        if(_buffers.get(_bufferIndex).remaining() == 0) 
        {
            _bufferIndex++;
            _position = 0;
        }
        else
        {
            _position++;
        }
        return returnByte;
    }

    /**
     *
     * Reads a certain data type from the file e.g. byte, Unsigned
     * Integer etc.
     *
     * @param dataType Type of data to read.
     * @return byte array of data.
     */
    private byte[] getValue(DataType dataType) {
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
            default:
                return null;
        }
        
        for(int i = 0; i < readSize; i++) {
            value[i] = getNextByte();
        }
        return value;
    }

    /**
     * Clears the ByteBuffers and closes the File Channel.
     */
    public void close() {
        for (ByteBuffer b : _buffers) {
            b.clear();
            b = null;
        }
        try {
            _triFile.close();
        } catch (IOException ex) {
            _logger.error("Failed to close Tri data file FileChannel: ", ex);
        }
    }

    /**
     * Defines what data type is being read from a given buffer.
     */
    private static enum DataType {

        Byte, Unsigned_Short, Unsigned_Int;
    }
}
