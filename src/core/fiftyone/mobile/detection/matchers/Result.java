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
package fiftyone.mobile.detection.matchers;

import fiftyone.mobile.detection.BaseDeviceInfo;
import fiftyone.mobile.detection.Constants;
import fiftyone.mobile.detection.Property;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.handlers.Handler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Contains a device matched via a handler.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class Result implements Comparable<Result> {

    /*
     * The provider used to create the result.
     */
    private final Provider _provider;
    /*
     * The primary device matched using the User-Agent HTTP header.
     */
    private final BaseDeviceInfo _primaryDevice;
    /*
     * The secondary device matched using a secondary HTTP header.
     */
    private BaseDeviceInfo _secondaryDevice;
    /*
     * The handler used to generate the result.
     */
    private final Handler _handler;
    /*
     * The score associated with the result. The lower the better.
     */
    private final long _score;
    /*
     * The useragent being sought.
     */
    private final String _userAgent;
    /*
     * The edit distance difference between the 
     * primary device's useragent and the one being
     * sought.
     */
    private int _difference;
    
    /*
     * True once the _difference value has been populated.
     */
    private boolean _differencePopulated = false;

    /**
     *
     * Constructs a new instance of Result.
     *
     * @param provider used to create the result.
     * @param primaryDevice matched using the User-Agent HTTP header.
     * @param secondaryDevice matched using a secondary HTTP header.
     * @param handler the handler used to generate the result.
     * @param score the score associated with the result.
     * @param userAgent the useragent being sought.
     */
    public Result(
            final Provider provider, 
            final BaseDeviceInfo primaryDevice, 
            final BaseDeviceInfo secondaryDevice, 
            final Handler handler, 
            final long score, 
            final String userAgent) {
        _provider = provider;
        _primaryDevice = primaryDevice;
        _secondaryDevice = secondaryDevice;
        _handler = handler;
        _score = score;
        _userAgent = userAgent;
    }
    
    /**
     *
     * Constructs a new instance of Result.
     *
     * @param provider used to create the result.
     * @param primaryDevice matched using the User-Agent HTTP header.
     * @param handler the handler used to generate the result.
     * @param score the score associated with the result.
     * @param userAgent the useragent being sought.
     */
    public Result(
            final Provider provider, 
            final BaseDeviceInfo primaryDevice, 
            final Handler handler, 
            final long score, 
            final String userAgent) {
        _provider = provider;
        _primaryDevice = primaryDevice;
        _handler = handler;
        _score = score;
        _userAgent = userAgent;
    }

    /**
     * 
     * @return The confidence of the result.
     */
    public int getConfidence()
    {
        return getDifference() == 0 ? Byte.MAX_VALUE : _handler.getConfidence();
    }

    /**
     * @return The edit distant indicator for the result.
     */
    public int getDifference()
    {
        if (_differencePopulated == false)
        {
            final String deviceUserAgent = _primaryDevice.getUserAgent();
            final int[][] rows = new int[deviceUserAgent.length() + 1][deviceUserAgent.length() + 1];
            _difference = Algorithms.EditDistance(rows, deviceUserAgent, _userAgent, Integer.MAX_VALUE);
            _differencePopulated = true;
        }
        return _difference;
    }

    /**
     * @return The score for the result.
     */
    public long getScore()
    {
        return _score;
    }

    /**
     * @return The device found from the user agent provided by the requesting information.
     */
    public BaseDeviceInfo getDevice()
    {
        return _primaryDevice;
    }

    /**
     * @return The device found using the UserAgent header User-Agent.
     */
    public BaseDeviceInfo getDevicePrimary()
    {
        return _primaryDevice;
    }

    /**
     * @return The device found using a secondary HTTP header. 
     */
    public BaseDeviceInfo getDeviceSecondary()
    {
        return _secondaryDevice;
    }

    /**
     * @return The handler used to obtain the result.
     */
    public Handler getHandler()
    {
        return _handler;
    }
    
    /**
    * If there are two devices, both a primary and secondary device,
    * this method works out which one should be used to return the 
    * property value.
    * 
    * @param propertyNameStringIndex the string index of the property being sought.
    * @returns the device that should be used to provide the property.
    */
    private BaseDeviceInfo getDeviceForProperty(final int propertyNameStringIndex)
    {
        if (_secondaryDevice != null)
        {
            final Property property = _provider.getProperties().get(propertyNameStringIndex);
            if (property != null)
            {
                switch(property.getComponent())
                {
                    case Hardware:
                    case Software:
                        return _secondaryDevice;
                    default:
                        return _primaryDevice;
                }
            }
        }
        return _primaryDevice;
    }
          
    /**
     * Sets the secondary device if one has been detected.
     * 
     * @param device the device to use as the secondary device for the result.
     */
    public void setSecondaryDevice(final BaseDeviceInfo device)
    {
        _secondaryDevice = device;
    }

    /**
     * Adds the device properties to the collection.
     * 
     * @param collection collection to have properties added to.
     */
    protected void addProperties(final Map<String,List<String>> collection)
    {
        for(int propertyStringIndex : _provider.getProperties().keySet())
        {
            final String property = _provider.getStrings().get(propertyStringIndex);
            if (Constants.EXCLUDE_PROPERTIES_FROM_ALL_PROPERTIES.contains(property) == false &&
                collection.containsKey(property) == false) {
                collection.put(
                    property,
                    getPropertyValues(propertyStringIndex));
            }
        }
    }

    /**
     * Gets the capability value index in the static Strings collection for this device
     * based on the index of the capability name. If this device does not have the 
     * value then checks the parent if one exists.
     * 
     * @param propertyStringIndex the string index of the property name.
     * @return Capability index value in the String collection, or null if the capability does not exist.
     */
    public List<Integer> getPropertyValueStringIndexes(final int propertyStringIndex)
    {
        return getDeviceForProperty(propertyStringIndex).getPropertyValueStringIndexes(propertyStringIndex);
    }

    /**
     * Returns the string index of the first element in the collection.
     * 
     * @param propertyStringIndex the string index of the property name.
     * @return Capability index value in the String collection, or -1 if the capability does not exist.
     */
    public int getFirstPropertyValueStringIndex(final int propertyStringIndex)
    {
        return getDeviceForProperty(propertyStringIndex).getFirstPropertyValueStringIndex(propertyStringIndex);
    }

    /**
     * Returns a list of the string values for the property index string provided.
     * 
     * @param propertyStringIndex the string index of the property name.
     * @return a list of string values
     */
    public List<String> getPropertyValues(final int propertyStringIndex)
    {
        return getDeviceForProperty(propertyStringIndex).getPropertyValues(propertyStringIndex);
    } 
    
    /**
     * Gets the first value for the property.
     * @param property name of the property to be returned.
     * @return value of the property.
     */
    public String getFirstPropertyValue(final String property)
    {
        final int index = getFirstPropertyValueStringIndex(_provider.getStrings().add(property));
        if (index >= 0) {
            return _provider.getStrings().get(index);
        }
        return null;
    }

    /**
     * Returns a list of the string values for the property.
     * 
     * @param property name of the property to be returned.
     * @return List of values for the property.
     */
    public List<String> getPropertyValues(final String property)
    {
        final int propertyStringIndex = _provider.getStrings().add(property);
        final List<String> values = new ArrayList<String>();
        final List<Integer> indexes = getPropertyValueStringIndexes(propertyStringIndex);
        if (indexes != null)
        {
            for(int index : indexes)
            {
                if (index >= 0) {
                    values.add(_provider.getStrings().get(index));
                }
            }
        }
        else
        {
            // Check for any special values not held in
            // the strings collection.
            if (Constants.DEVICE_ID.equals(property)) {
                values.add(
                    _secondaryDevice == null ?
                    _primaryDevice.getDeviceId() : 
                    _secondaryDevice.getDeviceId());
            }
        }
        return values;
    }

    /**
     * Joins the elements of the values array in to a single comma
     * separated string in a single array.
     * @param values a list of string values.
     * @return a single array list with one string containing a concatenated 
     * list of strings.
     */
    private ArrayList<String> joinValues(final List<String> values) {
        final ArrayList<String> result = new ArrayList<String>();
        final StringBuilder builder = new StringBuilder();
        for(String s : values) {
            builder.append(s);
            builder.append(", ");
        }
        result.add(builder.toString());
        return result;
    }
    
    /// </summary>
    /**
     * "
     * @return Returns a sorted list containing all the property values for the
     * the result.
     */
    public Map<String, List<String>> getAllProperties()
    {
        final Map<String, List<String>> collection = new HashMap<String, List<String>>();
        final List<String> initialList = new ArrayList<String>();
        
        initialList.add(
            _secondaryDevice == null ?
            _primaryDevice.getDeviceId() : 
            _secondaryDevice.getDeviceId());
        
        collection.put(Constants.DEVICE_ID, initialList);

        final ArrayList<String> handlerNames = new ArrayList<String>();
        for (Handler handler : _provider.getHandlers(
            _secondaryDevice == null ?
            _primaryDevice.getUserAgent() :
            _secondaryDevice.getUserAgent())) {
            handlerNames.add(handler.getName());
        }
        collection.put("Handlers", joinValues(handlerNames));
        
        final ArrayList<String> userAgents = new ArrayList<String>();
        userAgents.add(
            _secondaryDevice == null ?
            _primaryDevice.getUserAgent() : 
            _secondaryDevice.getUserAgent());
        collection.put("UserAgent", userAgents);

        addProperties(collection);
        return collection;
    }    
    
    /**
     *
     * Compare this instance to another Instance.
     *
     * @param r Instance for comparison.
     * @return Zero if equal. 1 if higher or -1 if lower.
     */
    @Override
    public int compareTo(final Result result) {
        if (this._primaryDevice == null && result.getDevice() == null) {
            return 0;
        }
        return _primaryDevice.getDeviceId().compareTo(result.getDevice().getDeviceId());
    }
}
