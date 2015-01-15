package fiftyone.mobile.detection;

import fiftyone.mobile.detection.entities.stream.TriePool;
import fiftyone.mobile.detection.entities.stream.TrieSource;
import fiftyone.mobile.detection.readers.TrieReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * Decision trie data structure provider.
 */
public class TrieProvider implements Disposable {

    /**
     * The type of integers used to represent the offset to the children.
     */
    public enum OffsetType {

        /**
         * The offsets in the node are 16 bit integers.
         */
        Bits16,
        /**
         * The offsets in the node are 32 bit integers.
         */
        Bits32,
        /**
         * The offsets in the node are 64 bit integers.
         */
        Bits64;

        public static OffsetType fromByte(byte value) {
            switch (value) {
                case 0:
                    return OffsetType.Bits16;
                case 1:
                    return OffsetType.Bits32;
                case 2:
                    return OffsetType.Bits64;
            }
            return null;
        }
    }
    private static final int SIZE_OF_LONG = 8,
            SIZE_OF_UINT = 4,
            SIZE_OF_INT = 4,
            SIZE_OF_USHORT = 2,
            SIZE_OF_SHORT = 2,
            SIZE_OF_UBYTE = 1,
            SIZE_OF_BYTE = 1;
    /**
     * The copy right notice associated with the data file.
     */
    public String Copyright;
    /**
     * Byte array of the Strings available.
     */
    private ByteBuffer _Strings;
    /**
     * Byte array of the available properties.
     */
    private ByteBuffer _properties;
    /**
     * Byte array of the devices list.
     */
    private ByteBuffer _devices;
    /**
     * Byte array of the look up list loaded into memory.
     */
    private short[] _lookupList;
    /**
     * A pool of readers that can be used in multi threaded operation.
     */
    private TriePool _pool;
    /**
     * The position in the source data file of the nodes.
     */
    private long _nodesOffset;
    /**
     * Dictionary of property names to indexes.
     */
    private final Map<String, Integer> _propertyIndex = new HashMap<String, Integer>();
    /**
     * List of the available property names.
     */
    private final List<String> _propertyNames = new ArrayList<String>();
    /**
     * The number of properties available in total.
     */
    private int _propertyCount = 0;

    /**
     * List of all property names for the provider.
     * @return list of all property names for the provider
     */
    public List<String> PropertyNames() {
        return _propertyNames;
    }

    /**
     * Constructs a new instance of a tree provider.
     *
     * @param copyright The copyright notice for the data file.
     * @param strings Array containing all Strings in the output.
     * @param properties Array of properties.
     * @param devices Array of devices.
     * @param lookupList Lookups data array.
     * @param nodesLength The length of the node data.
     * @param nodesOffset The position of the start of the nodes in the file
     * provided.
     * @param fileName Name of the source data file used to create the provider.
     * @throws FileNotFoundException indicates device data file was not found.
     */
    public TrieProvider(String copyright, byte[] strings, byte[] properties, byte[] devices,
            short[] lookupList, long nodesLength, long nodesOffset, String fileName) throws FileNotFoundException {
        Copyright = copyright;
        _Strings = ByteBuffer.wrap(strings);
        _properties = ByteBuffer.wrap(properties);
        _devices = ByteBuffer.wrap(devices);
        _lookupList = lookupList;
        _nodesOffset = nodesOffset;

        _Strings.order(ByteOrder.LITTLE_ENDIAN);
        _properties.order(ByteOrder.LITTLE_ENDIAN);
        _devices.order(ByteOrder.LITTLE_ENDIAN);

        // Creates a pool to use to access the source data file.
        _pool = new TriePool(new TrieSource(fileName));

        // Store the maximum number of properties.
        _propertyCount = properties.length / SIZE_OF_INT;

        // Get the names of all the properties.
        initPropertyNames();
    }

    /**
     * Returns the user agent matched against the one provided.
     *
     * @param userAgent user agent to match
     * @return user agent string that was matched against the user agent provided
     * @throws Exception indicates an exception occurred
     */
    public String getUserAgent(String userAgent) throws Exception {
        StringBuilder matchedUserAgent = new StringBuilder();
        TrieReader reader = _pool.getReader();
        reader.setPos(_nodesOffset);
        getDeviceIndex(
                reader,
                getUserAgentByteArray(userAgent),
                0,
                0,
                matchedUserAgent);
        _pool.release(reader);
        return matchedUserAgent.toString();
    }

    /**
     * Returns the index of the device associated with the given user agent. The
     * index returned may vary across different versions of the source data file
     * and should not be stored. The "Id" property will remain unique.
     *
     * @param userAgent user agent to get device index of.
     * @return index of the device associated with the given user agent.
     * @throws Exception indicates an exception occurred
     */
    public int getDeviceIndex(String userAgent) throws Exception {
        TrieReader reader = _pool.getReader();
        reader.setPos(_nodesOffset);
        int index = getDeviceIndex(
                reader,
                getUserAgentByteArray(userAgent),
                0,
                0);
        _pool.release(reader);
        return index;
    }

    /**
     * Returns the device id matching the device index.
     *
     * @param deviceIndex index of the device whose Id should be returned
     * @return device id matching the device index
     */
    public String getDeviceId(int deviceIndex) {
        return getPropertyValue(deviceIndex, "Id");
    }

    /**
     * Returns the property value based on the useragent provided.
     *
     * @param deviceIndex The index of the device whose property should be
     * returned.
     * @param property the name of the property required.
     * @return The value of the property for the given device index.
     */
    public String getPropertyValue(int deviceIndex, String property) {
        return getPropertyValue(deviceIndex, getPropertyIndex(property));
    }

    /**
     * Returns the value of the property index provided for the device index
     * provided.
     *
     * @param deviceIndex Index for the device.
     * @param propertyIndex Index of the property required.
     * @return value of the property index provided for the device index 
     * provided.
     */
    public String getPropertyValue(int deviceIndex, int propertyIndex) {
        int devicePosition = deviceIndex * _propertyCount * SIZE_OF_INT;
        int offset = devicePosition + (propertyIndex * SIZE_OF_INT);
        return getStringValue(_devices.getInt(offset));
    }

    /**
     * Returns the integer index of the property in the list of values
     * associated with the device.
     *
     * @param property property to return index of
     * @return integer index of the property in the list of values associated 
     * with the device.
     */
    public int getPropertyIndex(String property) {
        int index = -1;
        if (_propertyIndex.containsKey(property)) {

            index = _propertyIndex.get(property);
        } else {
            synchronized (_propertyIndex) {
                if (_propertyIndex.containsKey(property)) {
                    index = _propertyIndex.get(property);
                } else {
                    // Property does not exist in the cache, so find the index.
                    for (int i = 0; i < _propertyCount; i++) {
                        int offset = i * SIZE_OF_INT;
                        if (getStringValue(_properties.getInt(offset)).equals(property)) {
                            // The property has been found so store it, and return
                            // the index of the property.
                            index = i;
                            _propertyIndex.put(property, index);
                            break;
                        }
                    }
                }
            }
        }
        return index;
    }

    /**
     * Disposes of the pool assigned to the provider.
     */
    @Override
    public void dispose() {
        _pool.dispose();
    }

    /**
     * Initialises the full list of property names available from the provider.
     */
    private void initPropertyNames() {
        for (int i = 0; i < _propertyCount; i++) {
            int offset = i * SIZE_OF_INT;
            String value = getStringValue(_properties.getInt(offset));
            if (_propertyIndex.containsKey(value) == false) {
                _propertyIndex.put(value, i);
            }

            _propertyNames.add(~Collections.binarySearch(_propertyNames, value), value);
        }
    }

    /**
     * Returns the String at the offset provided.
     *
     * @param offset
     */
    private String getStringValue(int offset) {
        int index = 0;
        StringBuilder builder = new StringBuilder();
        byte current = _Strings.get(offset);
        while (current != 0) {
            builder.append((char) current);
            index++;
            current = _Strings.get(offset + index);
        }
        return builder.toString();
    }

    /**
     * Converts a user agent in to a null terminated byte array.
     *
     * @param userAgent The useragent to be tested.
     * @return A null terminated byte array.
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
     * Returns the offset in the node for the current character.
     *
     * @param lookupOffset The offset in the byte array
     * @param value The value to be checked.
     * @return The position to move to.
     */
    private short getChild(int lookupOffset, byte value) throws ArrayIndexOutOfBoundsException {
        try {
            short lowest = _lookupList[lookupOffset];
            short highest = _lookupList[lookupOffset + 1];
            if (value < lowest || value > highest) {
                return Byte.MAX_VALUE;
            }
            // Statement is equivalent to "(lookupOffset + value - lowest) + 2".
            int index = lookupOffset + value - lowest + 2;
            return _lookupList[index];
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw ex;
        }
    }

    /**
     * The number of bytes each offset takes.
     *
     * @param offsetType offset type
     * @return number of bytes each offset takes.
     */
    public static int sizeOfOffsets(OffsetType offsetType) {
        switch (offsetType) {
            case Bits16:
                return SIZE_OF_USHORT;
            case Bits32:
                return SIZE_OF_UINT;
            default:
                return SIZE_OF_LONG;
        }
    }

    /**
     * Returns the position in the nodes stream of the next node.
     *
     * @param reader Reader with exclusive access to the underlying file
     * @param childIndex
     * @param numberOfChildren
     * @param offsetType
     */
    private void setNextNodePosition(
            TrieReader reader,
            short childIndex,
            short numberOfChildren,
            OffsetType offsetType)
            throws IOException {
        if (childIndex == 0) {
            // Move past the children offset to the first child.
            long offset = reader.getPos() + (numberOfChildren - 1) * sizeOfOffsets(offsetType);
            reader.setPos(offset);
        } else {
            // Move to the bytes that represent the node position of the next node based on the child index.
            reader.setPos(reader.getPos() + (childIndex - 1) * sizeOfOffsets(offsetType));
            long pos = reader.getPos();
            switch (offsetType) {
                case Bits16:
                    pos += reader.readUShort();
                    break;
                case Bits32:
                    pos += reader.readUInt();
                    break;
                default:
                    pos += reader.readLong();
                    break;
            }
            reader.setPos(pos);
        }
    }

    /**
     * Returns the offset in the device byte array to the device matching the
     * useragent provided.
     *
     * @param reader Reader with exclusive access to the underlying file.
     * @param userAgent A null terminated byte array of the user agent to be
     * tested.
     * @param index The index in the array of the current character.
     * @param parentDeviceIndex The device index of the parent node.
     * @return The device id with the most number of matching characters.
     */
    private int getDeviceIndex(TrieReader reader, byte[] userAgent, int index, int parentDeviceIndex) throws Exception {
        // Position the reader for the nodePosition.

        // Get the lookup list.
        int lookupListOffset = reader.readInt();

        // Get the index of the child.
        int childIndex = getChild(Math.abs(lookupListOffset), userAgent[index]);

        // Get the index of the device.
        int deviceIndex;
        if (lookupListOffset >= 0) {
            // The lookup list is positive so the device index
            // is contained as the next 4 bytes.
            deviceIndex = reader.readInt();
        } else {
            // The look list is negative so the device index
            // of this node is the same as the parent device index.
            deviceIndex = parentDeviceIndex;
        }

        // If the child index indicates no children then
        // return the current device.
        if (childIndex == Byte.MAX_VALUE) {
            return deviceIndex;
        }

        // Get the number of children and check we're still within
        // the range for this node.
        byte numberOfChildren = reader.readByte();
        if (childIndex >= numberOfChildren) {
            return deviceIndex;
        }

        // If there's only 1 child then it will appear immediately after
        // this element. The position will already be set at that position.
        if (numberOfChildren == 1) {
            return getDeviceIndex(
                    reader,
                    userAgent,
                    index + 1,
                    deviceIndex);
        }

        // There's more than 1 child so find the integer type used for the
        // offset and then move to that position recognising the 1st child 
        // always appears at the position immediately after the list of children.
        OffsetType offsetType = OffsetType.fromByte(reader.readByte());
        setNextNodePosition(reader, (short) childIndex, numberOfChildren, offsetType);
        return getDeviceIndex(
                reader,
                userAgent,
                index + 1,
                deviceIndex);
    }

    /**
     * Returns the offset in the device byte array to the device matching the
     * useragent provided.
     *
     * @param reader Reader with exclusive access to the underlying file.
     * @param userAgent A null terminated byte array of the user agent to be
     * tested.
     * @param index The index in the array of the current character.
     * @param parentDeviceIndex The parent device index to be used if this node
     * doesn't have a different one.
     * @param matchedUserAgent">The characters of the user agent matched.
     * @return The device id with the most number of matching characters.
     */
    private int getDeviceIndex(TrieReader reader, byte[] userAgent, int index, int parentDeviceIndex, StringBuilder matchedUserAgent) throws Exception {
        // Add the character to the matched user agent.
        matchedUserAgent.append((char) userAgent[index]);

        // Get the lookup list.
        int lookupListOffset = reader.readInt();

        // Get the index of the child.
        int childIndex = getChild(Math.abs(lookupListOffset), userAgent[index]);

        // Get the index of the device.
        int deviceIndex;
        if (lookupListOffset >= 0) {
            // The lookup list is positive so the device index
            // is contained as the next 4 bytes.
            deviceIndex = reader.readInt();
        } else {
            // The look list is negative so the device index
            // of this node is the same as the parent device index.
            deviceIndex = parentDeviceIndex;
        }

        // If the child index indicates no children then
        // return the current device.
        if (childIndex == Byte.MAX_VALUE) {
            return deviceIndex;
        }

        // Get the number of children and check we're still within
        // the range for this node.
        byte numberOfChildren = reader.readByte();
        if (childIndex >= numberOfChildren) {
            return deviceIndex;
        }

        // If there's only 1 child then it will appear immediately after
        // this element. The position will already be set at that position.
        if (numberOfChildren == 1) {
            return getDeviceIndex(
                    reader,
                    userAgent,
                    index + 1,
                    deviceIndex,
                    matchedUserAgent);
        }

        // There's more than 1 child so find the integer type used for the
        // offset and then move to that position recognising the 1st child 
        // always appears at the position immediately after the list of children.
        OffsetType offsetType = OffsetType.fromByte(reader.readByte());
        setNextNodePosition(reader, (short) childIndex, numberOfChildren, offsetType);
        return getDeviceIndex(
                reader,
                userAgent,
                index + 1,
                deviceIndex,
                matchedUserAgent);
    }
}
