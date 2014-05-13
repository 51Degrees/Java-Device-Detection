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

import fiftyone.mobile.detection.handlers.Handler;
import fiftyone.mobile.detection.matchers.Result;
import fiftyone.mobile.detection.matchers.Results;
import fiftyone.mobile.detection.matchers.finalmatcher.Matcher;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Represents all device data and capabilities.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class Provider {
    /**
     * A list of handlers used to match devices.
     */
    private final List<Handler> _handlers = new ArrayList<Handler>();
    /**
     * Collection of all Strings used by the provider.
     */
    private final Strings _strings = new Strings();
    /**
     * SortedMap of all devices keyed on device hash code.
     */
    private final SortedMap<Integer, List<BaseDeviceInfo>> _allDevices =
            new TreeMap<Integer, List<BaseDeviceInfo>>();
    /**
     * A list of string indexes used for user agent profile properties.
     */
    private List<Integer> _userAgentProfileStringIndexes;
    /**
     * List of all the actual devices ignoring parents, or those children added
     * to handle User Agent strings.
     */
    private List<BaseDeviceInfo> _actualDevices = null;
    /**
     * Thread pool used when the application uses multi-threading. Only 
     * initialised if the multi threading is enabled by virtue of the number
     * of processors and the package not being set to single threaded operation.
     */
    private ThreadPoolExecutor _threadPool = null;
    /**
     * Set to true when the provider has been destroyed and the number of 
     * instances has been reduced.
     */    
    private boolean _destroyed = false;
    /*
     * A list of properties and associated values which can be returned by the
     * provider and it's data source.
     */
    private Map<Integer, Property> _properties = new HashMap<Integer, Property>();
    /*
     * The date and time the 1st data source used to create the provider was
     * created.
     */
    private Date _publishedDate = new Date();
    /**
     * The name of the data set the provider is using.
     */
    private String _dataSetName = "Unknown";
    /**
     * Creates a _logger for this class
     */
    private static final Logger _logger = LoggerFactory.getLogger(Provider.class);
    
     
    
    /**
     * Constructs a new instance of Provider. If this is the first instance
     * then a new thread pool will be created.
     */
    public Provider() {
        super();
        _logger.trace("Constructing provider");
    }
    
    /**
     * Constructs a new instance of Provider. If this is the first instance
     * then a new thread pool will be created.
     * 
     * @param threadPool for improved performance
     */
    public Provider(final ThreadPoolExecutor threadPool) {
        this();
        _threadPool = threadPool;
    }
    
    /**
     * @return Returns a list of the string indexes for user agent profile
     * properties.
     */
    public List<Integer> getUserAgentProfileStringIndexes() {
        if (_userAgentProfileStringIndexes == null) {
            _userAgentProfileStringIndexes = new ArrayList<Integer>();
            for (String value : Constants.USER_AGENT_PROFILES) {
                _userAgentProfileStringIndexes.add(_strings.add(value));
            }
        }
        return _userAgentProfileStringIndexes;
    }

    /**
     *
     * @return A list of handlers being used by the provider.
     */
    public List<Handler> getHandlers() {
        return _handlers;
    }

    /**
     *
     * @return Collection of all Strings used by the provider.
     */
    public Strings getStrings() {
        return _strings;
    }

    /**
     * SortedMap of all devices keyed on device hash code.
     */
    public SortedMap<Integer, List<BaseDeviceInfo>> getAllDevices() {
        return _allDevices;
    }
    
    /**
     * 
     * @return the ThreadPoolExecutor associated with this provider. Null if 
     * there is no ThreadPoolExecutor.
     */
    public ThreadPoolExecutor getThreadPool()
    {
        return _threadPool;    
    }

    /**
     *
     * A list of all devices available to the provider which have properties
     * assigned to them. This is needed to ignore devices which are only present
     * to represent a User Agent and do not have any properties assigned to
     * them.
     *
     * @return A list of all devices available to the provider which have
     * properties assigned to them.
     */
    public List<BaseDeviceInfo> getDevices() {
        if (_actualDevices == null) {
            _actualDevices = new ArrayList<BaseDeviceInfo>();
            for (Integer key : _allDevices.keySet()) {
                for (BaseDeviceInfo device : _allDevices.get(key)) {
                    if (device.getDeviceId().split("\\s*" + Constants.PROFILE_SEPERATOR + "\\s*").length == 4) {
                        _actualDevices.add(device);
                    }
                }
            }
        }
        return _actualDevices;
    }

    /**
     *
     * Find all the devices that match the request.
     *
     * @param headers List of http headers associated with the request.
     * @param threadPool The Provider Object's thread pool.
     * @return The closest matching device.
     */
    protected Results getMatches(final Map<String, String> headers) {
        // Get a User Agent string with common issues removed.
        String userAgent = getUserAgent(headers);
        if (userAgent != null) {
            if (!(userAgent.isEmpty())) {
                // Using the handler for this userAgent find the device.
                return getMatches(headers, getHandlers(headers));
            }
        }
        return null;
    }

    /**
     *
     * Find all the devices that match the User Agent String.
     *
     * @param userAgent String associated with the mobile device.
     * @param threadPool The Provider Object's thread pool.
     * @return The closest matching device.
     */
    protected Results getMatches(String userAgent) {
        if (userAgent != null) {
            if (!(userAgent.isEmpty())) {
                // Using the handler for this userAgent find the device.
                return getMatches(userAgent, getHandlers(userAgent));
            }
        }
        return null;
    }

    /**
     *
     * Use the HttpRequest fields to determine devices that match.
     *
     * @param headers Collection of Http headers associated with the request.
     * @param handlers Handlers capable of finding devices for the request.
     * @param threadPool The Provider Object's thread pool.
     * @return The closest matching device or null if one can't be found.
     */
    protected Results getMatches(Map<String, String> headers, Iterable<Handler> handlers) {
        Results results = new Results();
        try {
            for (Handler handler : handlers) {
                    // Find the closest matching devices.
                    Results temp = handler.match(headers);

                    // If some results have been found. Combine the results with
                    //results from previous handlers.
                    if (temp != null) 
                    {
                        results.addAll(temp);
                    }
                }
        } catch (InterruptedException ex) {
            LoggerFactory.getLogger(Provider.class).error("Error occured while iterating through handlers.", ex);
        }
        return results;
    }

    /**
     * Use the HttpRequest fields to determine devices that match.
     *
     * @param useragent The User Agent string of the device being sought.
     * @param handlers Handlers capable of finding devices for the request.
     * @return The closest matching device or null if one can't be found.
     */
    protected Results getMatches(String useragent, Iterable<Handler> handlers) {
        Results results = new Results();
        try {
            for (Handler handler : handlers) {
                    // Find the closest matching devices.
                    Results temp = handler.match(useragent);
                    // If some results have been found.
                    if (temp != null) // Combine the results with results from previous handlers.
                    {
                        results.addAll(temp);
                    }
                }
        } catch (InterruptedException ex) {
            LoggerFactory.getLogger(Provider.class).error("Error occured while iterating through handlers.", ex);
        }
        return results;
    }

    /**
     *
     * Using the unique device id returns the device. Very quick return when the
     * device id is known.
     *
     * @param deviceID Unique internal ID of the device
     * @return BaseDeviceInfo object.
     */
    public BaseDeviceInfo getDeviceInfoByID(final String deviceID) {
        List<BaseDeviceInfo> list = _allDevices.get(deviceID.hashCode());
        if (!list.isEmpty()) {
            // If only 1 element return this one.
            if (list.size() == 1) {
                return list.get(0);
            }
            // Return the first matching element.
            for (BaseDeviceInfo device : list) {
                if (device.getDeviceId().equals(deviceID)) {
                    return device;
                }
            }
        }
        return null;
    }

    /**
     *
     * Gets the handlers that will support the User Agent String.
     *
     * @param userAgent User Agent string associated with the HTTP request.
     * @return A list of all handlers that can handle this device.
     */
    public List<Handler> getHandlers(final String userAgent) {
        final Num highestConfidence = new Num();
        final List<Handler> handlers = new ArrayList<Handler>();
        for (Handler handler : _handlers) {
            if (handler.canHandle(userAgent)) {
                getHandlers(highestConfidence, handlers, handler);
            }
        }
        return handlers;
    }

    /**
     *
     * Returns an array of handlers that will be supported by the request. Is
     * used when a request is available so that header fields other than User
     * Agent can also be used in matching. For example; the User Agent Profile
     * fields.
     *
     * @param headers Collection of Http headers associated with the request.
     * @return An array of handlers able to match the request.
     */
    private List<Handler> getHandlers(final Map<String, String> headers) {
        final Num highestConfidence = new Num();
        final List<Handler> handlers = new ArrayList<Handler>();
        for (Handler handler : _handlers) {
            // If the handler can support the request and it's not the
            // catch all handler add it to the list we'll use for matching.
            if (handler.canHandle(headers)) {
                getHandlers(highestConfidence, handlers, handler);
            }
        }
        return handlers;
    }

    /**
     *
     * Adds the handler to the list of handlers if the handler's confidence is
     * higher than or equal to the current highest handler confidence.
     *
     * @param highestConfidence Highest confidence value to far.
     * @param handlers List of handlers.
     * @param handler Handler to be considered for adding.
     */
    private static void getHandlers(
            final Num highestConfidence, 
            final List<Handler> handlers, 
            final Handler handler) {
        if (handler.getConfidence() > highestConfidence.get()) {
            handlers.clear();
            handlers.add(handler);
            highestConfidence.set(handler.getConfidence());
        } else if (handler.getConfidence() == highestConfidence.get()) {
            handlers.add(handler);
        }
    }

    /**
     * Returns the main user agent string for the request.
     * 
     * @param headers Collection of Http headers associated with the request.
     * @return The main user agent string for the request
     */
    public static String getUserAgent(final Map<String, String> headers) {
        return headers.get(fiftyone.mobile.detection.Constants.USER_AGENT_HEADER);
    }
    
    /**
     * Used to check other header fields in case a device user agent is being used
     * and returns the devices useragent string.
     * 
     * @param headers Collection of Http headers associated with the request.
     * @return The useragent string of the device.
     */
    public static String getDeviceUserAgent(final Map<String, String> headers)
    {
        for (String current : fiftyone.mobile.detection.Constants.DEVICE_USER_AGENT_HEADERS) {
            if (headers.get(current) != null) {
                return headers.get(current);
            }
        }
        return null;
    }        
    
    /**
     *
     * @return A list of properties and associated values which can be returned
     * by the provider and it's data source.
     */
    public Map<Integer, Property> getProperties() {
        return _properties;
    }

    /**
     *
     * @return The date and time the data used to create the provider was
     * published.
     */
    public Date getPublishedDate() {
        return _publishedDate;
    }

    /**
     *
     * @return The name of the data set used to create the provider.
     */
    public String getDataSetName() {
        return _dataSetName;
    }

    /**
     *
     * Sets the name of the data set used to create the provider.
     *
     * @param name value to change data set name to.
     */
    public void setDataSetName(final String name) {
        this._dataSetName = name;
    }

    /**
     *
     * Sets the published date
     *
     * @param d A Date Object to be set to
     */
    public void setPublishedDate(final Date date) {
        _publishedDate = date;
    }

    /**
     *
     * @param userAgent User Agent string associated with the device to be
     * found.
     * @return An ArrayList of matching devices.
     */
    public List<BaseDeviceInfo> getMatchingDeviceInfo(final String userAgent) {
        List<BaseDeviceInfo> list = new ArrayList<BaseDeviceInfo>();
        try {
            final Results results = getMatches(userAgent);
            if (results != null) {
                for (Result result : results) {
                    list.add(result.getDevice());
                }
            }
        }
        catch(Exception ex) {
            _logger.error(
                "Fatal exception getting result for headers collection.",
                ex);
        }
        return list;
    }

    /**
     * Returns the device that most closely matches the headers
     * provided.
     * 
     * @param headers Collection of HTTP headers associated with the request.
     * @return The closest matching device.
     */
    public BaseDeviceInfo getDeviceInfo(final Map<String, String> headers) {
        return getDeviceInfo(getUserAgent(headers));
    }
    
    /**
     *
     * Gets the single most likely device to match the User Agent provided.
     *
     * @param userAgent User Agent string associated with the device to be
     * found.
     * @return The closest matching device.
     */
    public BaseDeviceInfo getDeviceInfo(final String userAgent) {
        final Result result = getResult(userAgent);
        if (result != null) {
            return result.getDevice();
        }
        return null;
    }
    
    /**
     * Returns the closest matching result for the user agent provided.
     * 
     * @param userAgent Useragent string associated with the device to be found.
     * @return The closest matching result.
     */
    public Result getResult(final String userAgent)
    {
        try {
            return getRequestClosestMatch(
                getMatches(userAgent), userAgent);
        }
        catch(Exception ex) {
            _logger.error(
                "Fatal exception getting result for headers collection.",
                ex);
            return null;
        }        
    }    
    
    /**
     * Returns the closest matching device from the result set to the target
     * userAgent.
     * 
     * @param results The result set to find a device from.
     * @param userAgent Target useragent.
     * @return The closest matching result.
     */
    private Result getRequestClosestMatch(
            final Results results, 
            final String userAgent)
    {
        if (results == null || results.size() == 0) {
            return null;
        }

        if (results.size() == 1) {
            return results.get(0);
        }
        
        Collections.sort(results);
        Result result = Matcher.matcher(userAgent, results);
        if (result != null) {
            return result;
        }
        return null;
    }   
    /**
     * Gets the closest matching result for the headers.
     * 
     * @param headers Collection of HTTP headers associated with the request.
     * The header keys must be given in lower case.
     * @return The closest matching result, or null if no result can be
     * determined.
     */
    public Result getResult(final Map<String, String> headers)
    {
        try {
            final Result result = getRequestClosestMatch(
                getMatches(headers), getUserAgent(headers));
            final String secondaryUserAgent = getDeviceUserAgent(headers);
            if (result != null &&
                secondaryUserAgent != null &&
                secondaryUserAgent.isEmpty() == false)
            {
                final BaseDeviceInfo secondaryDevice = getDeviceInfo(secondaryUserAgent);
                if (secondaryDevice != null) {
                    result.setSecondaryDevice(secondaryDevice);
                }
            }
            return result;
        }
        catch(Exception ex) {
            _logger.error(
                "Fatal exception getting result for headers collection.",
                ex);
            return null;
        }
    }    

    /**
     *
     * Returns all the devices that match the property and value passed into the
     * method.
     *
     * @param property The property required.
     * @param value The value the property must contain to be matched.
     * @return A list of matching devices. An empty list will be returned if no
     * matching devices are found.
     */
    public List<BaseDeviceInfo> findDevices(
            final String property, 
            final String value) {
        final List<BaseDeviceInfo> list = new ArrayList<BaseDeviceInfo>();
        final int propertyIndex = getStrings().add(property);
        final int requiredValueIndex = getStrings().add(value);
        for (BaseDeviceInfo device : getDevices()) {
            for (int valueIndex : device.getPropertyValueStringIndexes(propertyIndex)) {
                if (requiredValueIndex == valueIndex) {
                    list.add(device);
                }
            }
        }
        return list;
    }

    /**
     *
     * A list of devices based on the profile id provided.
     *
     * @param profileID The Profile ID of the devices required.
     * @return A list of matching devices. An empty list will be returned if no
     * matching devices are found.
     */
    public List<BaseDeviceInfo> findDevices(final String profileID) {
        final List<BaseDeviceInfo> list = new ArrayList<BaseDeviceInfo>();
        for (BaseDeviceInfo device : getDevices()) {
            for (String id : device.getProfileIDs()) {
                if (profileID.equals(id)) {
                    list.add(device);
                    break;
                }
            }
        }
        return list;
    }
}
