package fiftyone.mobile.detection.trie;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Class encapsulating a decision Trie data structure provider.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class Provider {

    /**
     * Creates a logger for this class
     */
    private static final Logger _logger = LoggerFactory.getLogger(Provider.class);
    
    /**
     * The size of the header in bytes where the following parameters are used.
     * 1. Lookup list offset. (long) 2. Device id index. (int) 3. Number of
     * children. (byte)
     */
    private static final long SIZE_OF_INT = 4, SIZE_OF_SHORT = 2, SIZE_OF_BYTE = 1;
    /**
     * Maximum size of an Unsigned Byte.
     */
    private static final byte BYTE_MAX = (byte) (255 & 0xFF);
    /**
     * Size of position lookup header.
     */
    private static final long HEADER_SIZE_BYTES = SIZE_OF_INT + SIZE_OF_SHORT + SIZE_OF_BYTE;
    /**
     * String lookup data.
     */
    protected final ByteBuffer Strings;
    /**
     * Properties lookup data.
     */
    protected final ByteBuffer Properties;
    /**
     * Devices lookup data.
     */
    protected final ByteBuffer Devices;
    /**
     * LookupList lookup data.
     */
    protected final ByteBuffer LookupList;
    /**
     * The trie data tree.
     */
    protected final FiftyOneMapper _nodesReader;
    /**
     * Value of the nodes offset.
     */
    protected final long _nodesOffset;
    /**
     * Dictionary of property names to indexes.
     */
    private final ConcurrentHashMap<String, Integer> _propertyIndex = new ConcurrentHashMap<String, Integer>();
    /**
     * The number of properties available in total.
     */
    private int _propertyCount = 0;

    /**
     *
     * Constructs a new instance of a tree provider.
     *
     * @param strings ByteBuffer containing all Strings in the output.
     * @param properties ByteBuffer of properties.
     * @param devices ByteBuffer Array of devices.
     * @param lookupList Lookups data array.
     * @param nodesReader Node data array.
     */
    public Provider(ByteBuffer strings, ByteBuffer properties, ByteBuffer devices,
            ByteBuffer lookupList, FileChannel nodesReader) throws IOException {
        Strings = strings;
        Properties = properties;
        Devices = devices;
        LookupList = lookupList;
        int rem = LookupList.remaining();
        // read length, nothing done with it
        nodesReader.position(nodesReader.position() + 4);
        _nodesOffset = nodesReader.position();
        _nodesReader = new FiftyOneMapper(nodesReader);

        // Store the maximum number of properties.
        _propertyCount = Properties.capacity() / (int) SIZE_OF_INT;
    }

    /**
     *
     * Checks if property offset has been looked up previously. If it has, the
     * index is returned. If it hasn't the index is calculated and stored, then
     * returned.
     *
     * @param property Property to search for.
     * @return Index in to use in property array.
     */
    private int getPropertyIndex(String property) {
        int index = -1;
        if (_propertyIndex.containsKey(property) == false) {
            // Property does not exist in the cache, so find the index.
            int StringIndex;
            for (int i = 0; i < _propertyCount; i++) {
                //StringIndex = BitConverter.ToInt32(Properties, i * SIZE_OF_INT);
                StringIndex = Properties.getInt(i * (int) SIZE_OF_INT);
                if (getStringValue(StringIndex).equals(property)) {
                    // The property has been found so store it, and return
                    // the index of the property.
                    index = i;
                    _propertyIndex.put(property, index);
                    break;
                }
            }
        } else {
            // If the propery does exist, retrun its value
            index = _propertyIndex.get(property);
        }
        return index;
    }

    /**
     *
     * Returns the device id matching the given User-Agent.
     *
     * @param userAgent User-Agent to match.
     * @return Device Id of the given User-Agent.
     */
    public String getDeviceId(String userAgent) {
        return getDeviceProperty(userAgent, "Id");
    }

    /**
     *
     * Returns the property value based on the user agent provided.
     *
     * @param userAgent The user agent to be tested.
     * @param property The name of the property required.
     * @return The device id of the matching device
     */
    public String getDeviceProperty(String userAgent, String property) {
        int deviceIndex = getDeviceIndex(
                getUserAgentByteArray(userAgent),
                0,
                0);
        return getPropertyValue(deviceIndex, getPropertyIndex(property));
    }

    /**
     *
     * Converts a user agent in to a null terminated byte array.
     *
     * @param userAgent The user agent to be tested
     * @return >A null terminated byte array
     */
    private static byte[] getUserAgentByteArray(String userAgent) {
        byte[] result = new byte[userAgent.length() + 1];
        for (int i = 0; i < userAgent.length(); i++) {
            result[i] = userAgent.charAt(i) <= 0x7F ? (byte) userAgent.charAt(i) : (byte) ' ';
        }
        result[result.length - 1] = 0;
        return result;
    }

    /**
     *
     * Returns the offset in the node for the current character.
     *
     * @param lookupOffset The offset in the byte array
     * @param value The value to be checked
     * @return The position to move to.
     */
    private byte getChild(long lookupOffset, byte value) {
        byte lowest = LookupList.get((int) lookupOffset);
        byte highest = LookupList.get((int) (lookupOffset + 1));
        if (value < lowest
                || value > highest) {
            return (byte) (255 & 0xFF);
        }

        // Statement equivalent to "(lookupOffset + value - lowest) + 2" 
        return LookupList.get((int) (lookupOffset + (long) value - (long) lowest + 2L));
    }

    /**
     * Returns the index of the lookup list.
     *
     * @param nodePosition Position of node.
     * @return Lookup list index.
     */
    private long getLookupOffset(long nodePosition) {
        return _nodesReader.readInt(_nodesOffset + nodePosition);
    }

    /**
     *
     * Returns the index of the device related to the current node.
     *
     * @param nodePosition Current node value.
     * @return Index of the device.
     */
    private int getDeviceIndex(long nodePosition) {
        return _nodesReader.readShort(_nodesOffset + nodePosition + SIZE_OF_INT);
    }

    /**
     *
     * Returns the number of children of a given node.
     *
     * @param nodePosition Node to use.
     * @return Number of child nodes.
     */
    private short getNumberOfChildren(long nodePosition) {
        return _nodesReader.readByte(_nodesOffset + nodePosition + SIZE_OF_INT + SIZE_OF_SHORT);
    }

    /**
     *
     * Returns the position in the nodes stream of the next node.
     *
     * @param nodePosition Current node position.
     * @param childIndex Index of nodes child.
     * @return Next nodes position.
     */
    private long getNextNodePosition(long nodePosition, byte childIndex) {
        return _nodesReader.readInt(_nodesOffset + nodePosition + HEADER_SIZE_BYTES + (childIndex * SIZE_OF_INT));
    }

    /**
     *
     * Returns the offset in the device byte array to the device matching the
     * user agent provided.
     *
     * @param userAgent A null terminated byte array of the user agent to be
     * tested
     * @param index The index in the array of the current character
     * @param nodePosition The current position in the byte stream for the start
     * of the node
     * @return The device id with the most number of matching characters
     */
    private int getDeviceIndex(byte[] userAgent, int index, long nodePosition) {
        // Get the index of the child.
        int childIndex = getChild(getLookupOffset(nodePosition), userAgent[index]);

        // If the child index indicates no children then
        // return the current device. Signified by unisigned 
        //byte max value
        if (childIndex == BYTE_MAX) {
            return getDeviceIndex(nodePosition);
        }

        // Get the number of children and check we're still within
        // the range for this node.
        short numberOfChildren = getNumberOfChildren(nodePosition);
        if (childIndex >= numberOfChildren) {
            return getDeviceIndex(nodePosition);
        }

        // Move to the child node in the byte array.
        return getDeviceIndex(
                userAgent,
                index + 1,
                getNextNodePosition(nodePosition, (byte) childIndex));
    }

    /**
     *
     * Returns the device id for the device offset provided.
     *
     * @param deviceIndex Offset for the device.
     * @param propertyIndex Index of the property required.
     * @return Device id value.
     */
    private String getPropertyValue(int deviceIndex, int propertyIndex) {
        int devicePosition = (deviceIndex * _propertyCount) * (int) SIZE_OF_INT;
        return getStringValue(Devices.getInt(devicePosition + (propertyIndex * (int) SIZE_OF_INT)));
    }

    /**
     *
     * Returns a String representation of the values at the offset provided.
     *
     * @param offset value of offset.
     * @return String at offset.
     */
    private String getStringValue(int offset) {
        int index = 0;
        StringBuilder builder = new StringBuilder();
        byte current = Strings.get(offset);
        while (current != 0) {
            builder.append((char) current);
            index++;
            current = Strings.get(offset + index);
        }
        return builder.toString();
    }

    /**
     * Finalize has been overridden to close the file chanel. Super is then
     * called to continue as normal.
     */
    @Override
    public void finalize() {
        try {
            _nodesReader.close();
            super.finalize();
        } catch (Throwable ex) {
            _logger.error("Failed to finalize Provider: ", ex);
        }
    }
}
