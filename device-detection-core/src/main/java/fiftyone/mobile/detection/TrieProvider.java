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
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */
package fiftyone.mobile.detection;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import fiftyone.mobile.detection.entities.stream.TriePool;
import fiftyone.mobile.detection.readers.TrieReader;
import java.io.Closeable;
import java.util.Map.Entry;

/**
 * Decision trie data structure provider.
 */
public abstract class TrieProvider implements Closeable {

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
    public String copyright;
    /**
     * Byte array of the Strings available.
     */
    private final ByteBuffer _Strings;
    /**
     * Byte array of the available properties.
     */
    protected ByteBuffer _properties;
    /**
     * Byte array of the devices list.
     */
    private final ByteBuffer _devices;
    /**
     * Byte array of the look up list loaded into memory.
     */
    private final short[] _lookupList;
    /**
     * A pool of readers that can be used in multi threaded operation.
     */
    private final TriePool pool;
    /**
     * The position in the source data file of the nodes.
     */
    private final long _nodesOffset;
    /**
     * Dictionary of property names to indexes.
     */
    protected final Map<String, Integer> _propertyIndex = 
            new HashMap<String, Integer>();
    /**
     * List of the available property names.
     */
    protected final List<String> _propertyNames = new ArrayList<String>();
    /**
     * List of HTTP headers for each property index.
     */
    protected final List<String[]> propertyHttpHeaders = 
            new ArrayList<String[]>();
    /**
     * A list of HTTP headers that the provider can use for device detection.
     */
    private volatile List<String> httpHeaders;
    /**
     * List of all property names for the provider.
     * @return list of all property names for the provider
     */
    public List<String> propertyNames() {
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
     * @param pool Pool connected to the data source.
     * @throws FileNotFoundException indicates device data file was not found.
     */
    public TrieProvider(String copyright, byte[] strings, byte[] properties, 
                        byte[] devices, short[] lookupList, long nodesLength, 
                        long nodesOffset, TriePool pool) 
                        throws FileNotFoundException {
        this.copyright = copyright;
        _Strings = ByteBuffer.wrap(strings);
        _properties = ByteBuffer.wrap(properties);
        _devices = ByteBuffer.wrap(devices);
        _lookupList = lookupList;
        _nodesOffset = nodesOffset;
        
        // Creates a pool to use to access the source data file.
        this.pool = pool;

        _Strings.order(ByteOrder.LITTLE_ENDIAN);
        _properties.order(ByteOrder.LITTLE_ENDIAN);
        _devices.order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Returns a list of Http headers that the provider can use for 
     * device detection.
     * @return a list of Http headers that the provider can use for 
     * device detection.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public List<String> getHttpHeaders() {
        List<String> localHttpHeaders = httpHeaders;
        if (localHttpHeaders == null) {
            synchronized(this) {
                localHttpHeaders = httpHeaders;
                if (localHttpHeaders == null) {
                    httpHeaders = localHttpHeaders = new ArrayList<String>();
                    for (String[] sa : propertyHttpHeaders) {
                        for (String s : sa) {
                            if (!httpHeaders.contains(s))
                                httpHeaders.add(s);
                        }
                    }
                }
            }
        }
        return localHttpHeaders;
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
        TrieReader reader = pool.getReader();
        reader.setPos(_nodesOffset);
        getDeviceIndex(
                reader,
                getUserAgentByteArray(userAgent),
                0,
                0,
                matchedUserAgent);
        pool.release(reader);
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
        int index;
        TrieReader reader = pool.getReader();
        try {
            reader.setPos(_nodesOffset);
            index = getDeviceIndex(
                    reader,
                    getUserAgentByteArray(userAgent),
                    0,
                    0
                    );
        } finally {
            pool.release(reader);
        }
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
     * Returns a collection of device indexes for each of the relevant HTTP 
     * headers provided. Those headers which are unrelated to device detection 
     * are ignored.
     * @param headers Collection of HTTP headers and values.
     * @return Collection of headers and device indexes for each one.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public Map<String, Integer> getDeviceIndexes( 
                                    final Map<String, String> headers ) throws 
                                                                IOException, 
                                                                Exception {
        Map<String, Integer> indexes = new TreeMap<String, Integer>();
        if (headers != null) {
            TrieReader reader = null;
            try {
                reader = pool.getReader();
                for (Entry entry : headers.entrySet()) {
                    String header = entry.getKey().toString();
                    if (getHttpHeaders().contains(header)) {
                        indexes.put(header, getDeviceIndex(headers.get(header)));
                    }
                }
            } finally {
                if (reader != null) {
                    pool.release(reader);
                }
            }
        } else {
            indexes.put("User-Agent", getDeviceIndex(null));
        }
        return indexes;
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
        return getPropertyValue(deviceIndex, _propertyIndex.get(property));
    }

    /**
     * Returns the property value based on the device indexes provided.
     * @param deviceIndexes Http headers and their device index.
     * @param property The name of the property required.
     * @return The value of the property for the given device index.
     */
    public String getPropertyValue(Map<String, Integer> deviceIndexes, 
                                   String property) {
        if (deviceIndexes == null || deviceIndexes.isEmpty()) {
            return getDefaultPropertyValue(property);
        }
        return getPropertyValue(deviceIndexes, _propertyIndex.get(property));
    }
    
    /**
     * Returns the default property value for a null User-Agent. Should be used 
     * when a property has been requested but the User-Agent or a collection of 
     * HTTP headers is null.
     * @param propertyName String representing name of the property required.
     * @return String with value associated with a specific property when the 
     * User-Agent Header is set to null.
     */
    public String getDefaultPropertyValue(String propertyName) {
        return getPropertyValue(0, propertyName);
    }
    
    /**
     * Returns the value of the property index provided from the device indexes 
     * provided. Matches the Http header to the property index.
     * @param deviceIndexes Indexes for the device.
     * @param propertyIndex Index of the property required.
     * @return The value of the property for the given device indexes.
     */
    public String getPropertyValue(Map<String, Integer> deviceIndexes, 
                                   int propertyIndex) {
        Integer deviceIndex;
        for (String header : propertyHttpHeaders.get(propertyIndex)) {
            deviceIndex = deviceIndexes.get(header);
            if (deviceIndex != null) {
                return getPropertyValue(deviceIndex, propertyIndex);
            }
        }
        return null;
    }
    
    /**
     * Returns the value of the property index provided for the device index
     * provided.
     *
     * @param deviceIndex Index for the device.
     * @param propertyIndex Index of the property required.
     * @return The value of the property index for the given device index.
     */
    public String getPropertyValue(int deviceIndex, int propertyIndex) {
        int devicePosition = deviceIndex * _propertyNames.size() * SIZE_OF_INT;
        int offset = devicePosition + (propertyIndex * SIZE_OF_INT);
        return getStringValue(_devices.getInt(offset));
    }

    /**
     * Returns the value of the property for the user agent provided.
     * @param headers Collection of HTTP headers and values.
     * @param propertyName Name of the property required.
     * @return The value of the property for the given user agent.
     * @throws java.lang.Exception if there was a problem accessing data file.
     */
    public String getPropertyValueWithMultiHeaders( Map<String, String> headers, 
                                                    String propertyName ) 
                                                    throws Exception {
        return getPropertyValue(getDeviceIndexes(headers), propertyName);
    }
    
    /**
     * Returns the value of the property for the user agent provided.
     * @param userAgent User agent of the request.
     * @param propertyname Name of the property required.
     * @return The value of the property for the given user agent.
     * @throws Exception if there was a problem accessing data file.
     */
    public String getPropertyValue(String userAgent, String propertyname) 
                                                            throws Exception {
        return getPropertyValue(getDeviceIndex(userAgent), propertyname);
    }
    
    /**
     * Disposes of the pool assigned to the provider.
     */
    @Override
    public void close() {
        pool.close();
    }

    /**
     * Returns the String at the offset provided.
     *
     * @param offset integer offset.
     * @return value as a string.
     */
    protected String getStringValue(int offset) {
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
     * @param userAgent The User-Agent to be tested.
     * @return A null terminated byte array.
     */
    @SuppressWarnings("null")
    private static byte[] getUserAgentByteArray(String userAgent) {
        byte[] result = new byte[userAgent != null ? userAgent.length() + 1 : 0];
        if (result.length > 0) {
            for (int i = 0; i < userAgent.length(); i++) {
                result[i] = 
                        userAgent.charAt(i) <= 0x7F ? (byte) userAgent.charAt(i) : (byte) ' ';
            }
            result[result.length - 1] = 0;
        }
        return result;
    }

    /**
     * Returns the offset in the node for the current character.
     *
     * @param lookupOffset The offset in the byte array
     * @param value The value to be checked.
     * @return The position to move to.
     */
    private short getChild(int lookupOffset, byte value) 
                                        throws ArrayIndexOutOfBoundsException {
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
            throw new WrappedIOException(ex.getMessage());
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
            long offset = 
                    reader.getPos() + (numberOfChildren - 1) * sizeOfOffsets(offsetType);
            reader.setPos(offset);
        } else {
            // Move to the bytes that represent the node position of the next 
            // node based on the child index.
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
    private int getDeviceIndex(TrieReader reader, byte[] userAgent, int index, 
                               int parentDeviceIndex) throws Exception {
        // Position the reader for the nodePosition.

        // Get the lookup list.
        int lookupListOffset = reader.readInt();
        
        // If there are no more characters in the user agent return 
        // the parent device index.
        if (index == userAgent.length)
            return parentDeviceIndex;

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
    private int getDeviceIndex(TrieReader reader, byte[] userAgent, int index, 
                               int parentDeviceIndex, 
                               StringBuilder matchedUserAgent) throws Exception {
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
