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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * A possible value associated with a property.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class Value extends PropertyValue implements Comparable<Value> {

    /**
     * The property the value is associated with.
     */
    final private Property _property;
    /**
     * A list of devices associated with the value.
     */
    private List<BaseDeviceInfo> _devices = null;

    /**
     *
     * Constructs an instance of Value.
     *
     * @param property The property the value is associated with.
     * @param name The String name.
     */
    Value(final Property property, final String name) {
        super(property.getProvider(), name);
        _property = property;
    }

    /**
     *
     * Constructs an instance of Value.
     *
     * @param property The property the value is associated with.
     * @param name The String name.
     * @param description The description of the name.
     */
    Value(final Property property, final String name, final String description) {
        super(property.getProvider(), name, description);
        _property = property;
    }

    /**
     *
     * Constructs an instance of Value.
     *
     * @param property The property the value is associated with.
     * @param name The string name.
     * @param description The description of the name.
     * @param url An optional URL linking to more information about the name.
     */
    public Value(
            final Property property, 
            final String name, 
            final String description, 
            final String url) {
        super(property.getProvider(), name, description, url);
        _property = property;
    }

    /**
     *
     * @return The property the value is associated with.
     */
    public Property getProperty() {
        return _property;
    }

    /**
     *
     * @return A list of devices associated with the value.
     */
    public List<BaseDeviceInfo> devices() {
        if (_devices == null) {
            _devices = new ArrayList<BaseDeviceInfo>();
            for (BaseDeviceInfo device : getProvider().getDevices()) {
                // Get all the values this property has for the current device.
                for (int index : device.getPropertyValueStringIndexes(_property.getNameStringIndex())) {
                    // If the value of the device property and this value match 
                    // then add the device to the list and move to the next device.
                    if (index == getNameStringIndex()) {
                        _devices.add(device);
                        break;
                    }
                }
            }
        }
        return _devices;
    }

    @Override
    public int compareTo(final Value other) {
        return getName().compareTo(other.getName());
    }
}
