/* *********************************************************************
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

import fiftyone.mobile.detection.matchers.segment.Segment;
import java.util.*;

/**
 * Represents a device and holds all its settings.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class BaseDeviceInfo {

    /**
     * A list of child devices.
     */
    final private List<BaseDeviceInfo> _children = new ArrayList<BaseDeviceInfo>();
    /**
     * A list of the active children for the device.
     */
    private List<BaseDeviceInfo> _activeChildren = null;
    /**
     * The parent device.
     */
    private BaseDeviceInfo _parent;
    /**
     * Holds all properties from the current device.
     */
    private Collection _deviceProperties;
    /**
     * The unique Id of the device.
     */
    private String _deviceId;
    /**
     * A reference to the provider associated with this device.
     */
    protected Provider _provider;
    /**
     * The User Agent string of the device.
     */
    private String _userAgent;
    /**
     * A list of the profile IDs which make up the device.
     */
    private List<String> _profileIDs = null;
    /**
     * A collection of handler specific data.
     */
    private Map<fiftyone.mobile.detection.handlers.Handler, List<List<Segment>>> _handlerData;

    /**
     * Contains the device User Agent string.
     *
     * @return The String value of the User Agent.
     */
    public String getUserAgent() {
        return _userAgent;
    }

    /**
     * Update the parent, ensuring the child collection is also updated.
     *
     * @param value Object referencing the parent of this object.
     */
    public void setParent(final BaseDeviceInfo value) {
        if (_parent != null) {
            _parent._children.remove(this);
            _parent._activeChildren = null;
        }
        _parent = value;
        if (_parent != null) {
            _parent._children.add(this);
        }
    }

    /**
     * Gets the unique identifier of the device. Made up of the profile IDs
     * related to the device in a hyphen delimited String.
     *
     * @return String containing the Device ID.
     */
    public String getDeviceId() {
        return _deviceId;
    }

    /**
     *
     * Returns the profile ID Strings which make up the device ID.
     *
     * @return Pipe('|') delimited String of profile IDs.
     */
    public List<String> getProfileIDs() {
        if (_profileIDs == null) {
            _profileIDs.addAll(Arrays.asList(getDeviceId().split("\\s*" + Constants.PROFILE_SEPERATOR + "\\s*|\\s* \\s*")));
        }
        return _profileIDs;
    }

    /**
     *
     * @return The provider associated with the device.
     */
    public Provider getProvider() {
        return _provider;
    }

    /**
     * Internal accessor for the User Agent String.
     *
     * @param value value for the internal User Agent.
     */
    void setInternalUserAgent(final String value) {
        _userAgent = value;
    }

    /**
     *
     * A list of properties for the device for internal package use.
     * Properties are expressed as integer indexes in the string 
     * collection which is meaningless to external callers.
     *
     * @return A Collection of all device properties and values as string
     * indexes.
     */
    public Collection getStringIndexedProperties() {
        return _deviceProperties;
    }

    /**
     *
     * @return The data object for the specific handler.
     */
    private Map<fiftyone.mobile.detection.handlers.Handler, List<List<Segment>>> getHandlerData() {
        if (_handlerData == null) {
            _handlerData = new HashMap<fiftyone.mobile.detection.handlers.Handler, List<List<Segment>>>();
        }
        return _handlerData;

    }

    /**
     * Returns the handlers data ArrayList. If no data has been created for the
     * handler then new data is created.
     *
     * @param handler The handler related to the device.
     * @return The existing ArrayList, or a new instance.
     */
    public List<List<Segment>> getHandlerData(final fiftyone.mobile.detection.handlers.Handler handler) {

        List<List<Segment>> data;
        if (getHandlerData().containsKey(handler)) {
            data = getHandlerData().get(handler);
        } else {
            data = new ArrayList<List<Segment>>();
            getHandlerData().put(handler, data);
        }

        return data;
    }

    /**
     *
     * Creates an instance of BaseDeviceInfo.
     *
     * @param devices A reference to the complete index of devices.
     * @param deviceId A unique Identifier of the device.
     * @param userAgent User agent String used to identify this device.
     * @param parent The parent device if one exists.
     */
    public BaseDeviceInfo(final Provider devices, 
            final String deviceId, 
            final String userAgent, 
            final BaseDeviceInfo parent) {
        init(devices, deviceId, userAgent, parent);
    }

    /**
     *
     * Creates an instance of BaseDeviceInfo.
     *
     * @param devices A reference to the complete index of devices.
     * @param deviceId A unique Identifier of the device.
     * @param userAgent User agent string used to identify this device.
     */
    public BaseDeviceInfo(final Provider devices, 
            final String deviceId, 
            final String userAgent) {
        init(devices, deviceId, userAgent);
    }

    /**
     *
     * Creates an instance of DeviceInfo.
     *
     * @param devices A reference to the complete index of devices.
     * @param deviceId A unique Identifier of the device.
     */
    public BaseDeviceInfo(final Provider devices, final String deviceId) {
        init(devices, deviceId);
    }

    /**
     *
     * Method used by the constructor to initialise the object.
     *
     * @param devices A reference to the complete index of devices.
     * @param deviceId A unique Identifier of the device.
     */
    private void init(final Provider devices, final String deviceId) {

        if (deviceId == null || 
            deviceId.equals("")) {
            throw new IllegalArgumentException("deviceId");
        }

        if (devices == null) {
            throw new IllegalArgumentException("devices");
        }

        _provider = devices;
        _deviceId = deviceId;
        _deviceProperties = new Collection(devices.getStrings());
    }

    /**
     *
     * Method used by the constructor to initialise the object.
     *
     * @param devices A reference to the complete index of devices.
     * @param deviceId A unique Identifier of the device.
     * @param userAgent User agent String used to identify this device.
     * @param parent The parent device if one exists.
     */
    private void init(final Provider devices, 
            final String deviceId, 
            final String userAgent,
            final BaseDeviceInfo parent) {
        setParent(parent);
        init(devices, deviceId, userAgent);
    }

    /**
     *
     * Method used by the constructor to initialise the object.
     *
     * @param devices A reference to the complete index of devices.
     * @param deviceId A unique Identifier of the device.
     * @param userAgent User agent String used to identify this device.
     */
    private void init(final Provider devices, 
            final String deviceId, 
            final String userAgent) {
        _userAgent = userAgent;
        init(devices, deviceId);
    }

    /**
     *
     * @param device The device being checked.
     * @return True if the device is within the parent hierarchy of the device.
     */
    private boolean getIsParent(final BaseDeviceInfo device) {
        if (this == device) {
            return true;
        }
        if (_parent != null) {
            return _parent.getIsParent(device);
        }
        return false;
    }

    /**
     *
     * Gets the capability value index in the static Strings collection for this
     * device based on the index of the capability name. If this device does not
     * have the value then checks the parent if one exists.
     *
     * @param index Capability name index.
     * @return Capability index value in the String collection, or null if the
     * capability does not exist.
     */
    public List<Integer> getPropertyValueStringIndexes(final int index) {
        final List<Integer> value = _deviceProperties.get(index);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        if (_parent != null) {
            return _parent.getPropertyValueStringIndexes(index);
        }
        return null;
    }

    /**
     *
     * @param index Capability name index.
     * @return The string index of the first element in the collection.
     */
    public int getFirstPropertyValueStringIndex(final int index) {
        final List<Integer> value = getPropertyValueStringIndexes(index);
        if (value != null && !value.isEmpty()) {
            return value.get(0);
        }
        return -1;
    }

    /**
     *
     * @param propertyStringIndex The string index of the property name.
     * @return A list of the string values for the property index string
     * provided.
     */
    public List<String> getPropertyValues(final int propertyStringIndex) {
        final List<String> values = new ArrayList<String>();
        final List<Integer> gpvsi = getPropertyValueStringIndexes(propertyStringIndex);
        if (gpvsi != null) {
            for (int index : gpvsi) {
                if (index >= 0) {
                    values.add(getProvider().getStrings().get(index));
                }
            }
        }
        return values;
    }

    /**
     *
     * Adds the device properties to the collection.
     *
     * @param collection Collection to have properties added to.
     */
    protected void addProperties(Map<String, List<String>> collection) {
        for (int propertyStringIndex : _deviceProperties.keySet()) {
            final String property = _provider.getStrings().get(propertyStringIndex);
            if (!Constants.EXCLUDE_PROPERTIES_FROM_ALL_PROPERTIES.contains(property)
                    && !collection.containsKey(property)) {
                collection.put(
                        property,
                        getPropertyValues(propertyStringIndex));
            }
        }
        if (_parent != null) {
            _parent.addProperties(collection);
        }
    }

    /**
     *
     * Gets the value of the first value for the property.
     *
     * @param property the property you want to find.
     * @return the first value relating to that property.
     */
    public String getFirstPropertyValue(String property) {
        int index = getFirstPropertyValueStringIndex(_provider.getStrings().add(property));
        if (index >= 0) {
            return _provider.getStrings().get(index);
        }

        // Check for any special values not held in
        // the strings collection.
        if (property.equals(Constants.DEVICE_ID)) {
            return _deviceId;
        }

        return null;
    }

    /**
     *
     * Returns a list of the string values for the property.
     *
     * @param property the property you want to find.
     * @return All Values relating to that property.
     */
    public List<String> getPropertyValues(final String property) {
        final List<String> values = new ArrayList<String>();
        final List<Integer> indexes = getPropertyValueStringIndexes(_provider.getStrings().add(property));
        if (indexes == null) {// Check for any special values not held in
            // the strings collection.
            if (property.equals(Constants.DEVICE_ID)) {
                values.add(_deviceId);
            }
        } else {
            for (int index : indexes) {
                if (index >= 0) {
                    values.add(_provider.getStrings().get(index));
                }
            }
        }
        return values;
    }

    /**
     *
     * @return Returns a sorted list containing all the property values for the
     * the device.
     */
    public Map<String, List<String>> getAllProperties() {
        final Map<String, List<String>> collection = new HashMap<String, List<String>>();

        collection.put(Constants.DEVICE_ID, Arrays.asList(_deviceId));

        final List<String> handlerNames = new ArrayList<String>();
        for (fiftyone.mobile.detection.handlers.Handler handler : _provider.getHandlers(getUserAgent())) {
            handlerNames.add(handler.getName());
        }
        
        final StringBuilder join = new StringBuilder();
        for (String name : handlerNames) {
            join.append(name);
            join.append(", ");
        }

        collection.put("Handlers", Arrays.asList(join.toString()));
        collection.put("UserAgent", Arrays.asList(_userAgent));
        addProperties(collection);
        return collection;
    }

    /**
     *
     * Checks if another BaseDeviceInfo is equal to this one.
     *
     * @param other Other BaseDeviceInfo object.
     * @return True if the object instances are the same.
     */
    private boolean equals(final BaseDeviceInfo other) {
        return _deviceId.equals(other.getDeviceId())
                && _userAgent.equals(other.getUserAgent())
                && _deviceProperties.equals(other.getStringIndexedProperties())
                && capabilitiesEquals(other);
    }

    /**
     *
     * Check the strings are all equal.
     *
     * @param other Other BaseDeviceInfo object.
     * @return True if the object capability strings are the same.
     */
    private boolean capabilitiesEquals(final BaseDeviceInfo other) {
        for (int key : _deviceProperties.keySet()) {
            if (!_provider.getStrings().get(key).equals(other.getProvider().getStrings().get(key))) {
                return false;
            }
            for (int value : getPropertyValueStringIndexes(key)) {
                if (!_provider.getStrings().get(value).equals(other.getProvider().getStrings().get(value))) {
                    return false;
                }
            }
        }
        return true;
    }
}
