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
package fiftyone.mobile.detection.handlers;

import fiftyone.mobile.detection.BaseDeviceInfo;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.matchers.Results;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Base handler class for device detection.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public abstract class Handler implements Comparable<Handler> {

    /**
     * The default confidence to assign to results from the handler.
     */
    final private static int DEFAULT_CONFIDENCE = 5;
    /**
     * HTTP headers containing uaprof urls.
     */
    final private static String[] UAPROF_HEADERS = new String[]{
        "profile",
        "x-wap-profile",
        "X-Wap-Profile"
    };
    /**
     * A collection of domain names used with uaprof urls.
     */
    final private List<String> _uaProfDomains = new ArrayList<String>();
    /**
     * A single collection of all uaprof urls used by devices assigned to this
     * handler.
     */
    final private Map<Integer, List<BaseDeviceInfo>> _uaprofs =
            new HashMap<Integer, List<BaseDeviceInfo>>();
    /**
     * A single collection of all devices assigned to this handler, Keyed on the
     * hashcode of the User Agent.
     */
    final private Map<Integer, List<BaseDeviceInfo>> _devices =
            new HashMap<Integer, List<BaseDeviceInfo>>();
    /**
     * The name of the handler for debugging purposes.
     */
    private String _name = null;
    /**
     * The confidence matches from this handler should be given compared to
     * other handlers.
     */
    private int _confidence = 0;
    /**
     * True if the UA Prof HTTP headers should be checked.
     */
    private boolean _checkUAProfs = false;
    /**
     * A list of regular expression that if matched will indicate support for
     * this handler.
     */
    final private List<HandleRegex> _canHandleRegex = new ArrayList<HandleRegex>();
    /**
     * A list of regular expression that if matched indicate the handler is not
     * supported.
     */
    final private List<HandleRegex> _cantHandleRegex = new ArrayList<HandleRegex>();
    /**
     * The provider instance the handler is associated with.
     */
    final protected Provider _provider;
    /**
     * Regular expression used to extract a url host.
     */
    final private static Pattern _urlHost =
            Pattern.compile("^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$");

    /**
     *
     * @return The name of the handler for debugging purposes.
     */
    public String getName() {
        return _name;
    }

    /**
     *
     * @return true/false depending on if the UA Profs should be checked.
     */
    public boolean getCheckUAProfs() {
        return _checkUAProfs;
    }

    /**
     *
     * @return The confidence to assign to results from this handler.
     */
    public int getConfidence() {
        return _confidence;
    }

    /**
     *
     * @return A list of regexs that if matched indicate the handler does
     * support the User Agent passed to it.
     */
    public List<HandleRegex> getCanHandleRegex() {
        return _canHandleRegex;
    }

    /**
     *
     * @return A list of regexs that if matched indicate the handler does not
     * support the User Agent passed to it.
     */
    public List<HandleRegex> getCantHandleRegex() {
        return _cantHandleRegex;
    }

    /**
     *
     * @return The provider the handler is associated to.
     */
    Provider getProvider() {
        return _provider;
    }

    /**
     *
     * @return The list of devices assigned to the handler keyed on the User
     * Agent's hashcode.
     */
    Map<Integer, List<BaseDeviceInfo>> getUAProfs() {
        return _uaprofs;
    }

    /**
     *
     * @return Returns the list of devices assigned to the handler keyed on the
     * User Agent's hashcode.
     */
    public synchronized Map<Integer, List<BaseDeviceInfo>> getDevices() {
        return _devices;
    }

    /**
     *
     * Constructs an instance of Handler.
     *
     * @param provider Reference to the provider instance the handler will be
     * associated with.
     * @param name Name of the handler for debugging purposes.
     * @param confidence The confidence this handler should be given compared to
     * others.
     * @param checkUAProfs True if UAProfs should be checked.
     */
    Handler(
            final Provider provider, 
            final String name, 
            final int confidence, 
            final boolean checkUAProfs) {

        //If null or empty then throw an exception
        if (name == null) {
            throw new IllegalArgumentException(name);
        }
        if (name.equals("")) {
            throw new IllegalArgumentException(name);
        }
        _provider = provider;
        _name = name;
        _confidence = confidence > 0 ? confidence : DEFAULT_CONFIDENCE;
        _checkUAProfs = checkUAProfs;
    }
    
    /**
     *
     * The inheriting classes match method.
     *
     * @param userAgent The User Agent to match.
     * @return A result set of matching devices.
     */
    public abstract Results match(final String userAgent) throws InterruptedException;

    /**
     *
     * Compares this handler to another where the handler is used as a key in a
     * sorted list.
     *
     * @param other The other handler to compare to this one.
     * @return the value 0 if the argument string is equal to this string. a
     * value less than 0 if this string is lexicographically less than the
     * string argument; and a value greater than 0 if this string is
     * lexicographically greater than the string argument.
     */
    @Override
    public int compareTo(final Handler other) {
        return getName().compareTo(other.getName());
    }

    /**
     *
     * Returns true or false depending on the handlers ability to match the User
     * Agent provided.
     *
     * @param userAgent The User Agent to be tested.
     * @return true if this handler can support the User Agent, otherwise false.
     */
    public boolean canHandle(final String userAgent) {
        for (HandleRegex regex : _cantHandleRegex) {
            if (regex.isMatch(userAgent)) {
                return false;
            }
        }

        for (HandleRegex regex : _canHandleRegex) {
            if (regex.isMatch(userAgent)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * Adds a new device to the handler.
     *
     * @param device Device being added to the handler.
     */
    public void set(final BaseDeviceInfo device) {
        setUserAgent(device);
        setUaProf(device);
    }

    /**
     *
     * Returns the device matching the userAgent string if one is available.
     *
     * @param userAgent User Agent being sought.
     * @return null if no device is found. Otherwise the matching device.
     */
    BaseDeviceInfo getDeviceInfo(String userAgent) {
        // Get the devices with the same hashcode as the useragent.
        final List<BaseDeviceInfo> devices = getDeviceInfo(_devices, userAgent);
        if (devices != null && devices.size() > 0) {
            // If only one device available return this one.
            if (devices.size() == 1) {
                return devices.get(0);
            }

            // Look at each device for an exact match. Very rare
            // that more than one device will be returned.
            for (BaseDeviceInfo device : devices) {
                if (device.getUserAgent().equals(userAgent)) {
                    return device;
                }
            }
        }
        return null;
    }

    /**
     *
     * Returns all the devices that match the UA prof provided.
     *
     * @param uaprof UA prof to search for.
     * @return Results containing all the matching devices.
     */
    Results getResultsFromUAProf(final String uaprof) {
        final List<BaseDeviceInfo> devices = getDeviceInfo(_uaprofs, uaprof);
        if (devices != null && devices.size() > 0) {
            // Add the devices to the list of results and return.
            final Results results = new Results();
            BaseDeviceInfo[] d = null;
            devices.toArray(d);
            results.addRange(d, this, 0, "");
            return results;
        }
        return null;
    }

    /**
     *
     * Checks to see if the handler can support this device.
     *
     * @param device Device to be checked.
     * @return True if the device is supported, other false.
     */
    protected boolean canHandle(final BaseDeviceInfo device) {
        return canHandle(device.getUserAgent());
    }

    /**
     *
     * First checks if the User Agent from the request can be handled by this
     * handler.
     *
     * If the User Agent can't be handled then the request is checked to
     * determine if a uaprof header field is provided. If so we check the list
     * of uaprof domains assigned to this handler to see if they share the same
     * domain.
     *
     * @param headers Collection of http headers.
     * @return True if this handler could be able to match the device otherwise
     * false.
     */
    public boolean canHandle(final Map<String, String> headers) {
        final boolean canHandle = canHandle(Provider.getUserAgent(headers));
        if (_checkUAProfs && canHandle == false && _uaProfDomains.size() > 0) {
            String url = null;
            for (String header : UAPROF_HEADERS) {
                final String value = (String) headers.get(header);
                final Matcher m = _urlHost.matcher(value);
                if (m.find()) {
                    url = m.group();
                }
                if (value != null && _uaProfDomains.contains(url)) {
                    return true;
                }
            }
        }
        return canHandle;
    }
    
    private Results matchExact(
            final String userAgent, 
            final Map<String, String> headers) {
        // Check for an exact match of the user agent string.
        final BaseDeviceInfo device = getDeviceInfo(userAgent);
        if (device != null) {
            return new Results(device, this, 0, userAgent);
        }

        // Check to see if we have a uaprof header parameter that will produce
        // an exact match.
        if (_checkUAProfs && headers != null && !headers.isEmpty()) {
            for (String header : UAPROF_HEADERS) {
                String value = (String) headers.get(header);
                if (value != null) {
                    if (!(value.equals(""))) {
                        value = cleanUserAgentProfileUrl(value);
                        final Results results = getResultsFromUAProf(value);
                        if (results != null && results.size() > 0) {
                            return results;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     *
     * Performs an exact match using the userAgent string. If no results are
     * found uses the UA prof header parameters to find a list of devices.
     *
     * @param headers Collection of Http headers associated with the request.
     * @return null if no exact match was found. Otherwise the matching devices.
     */
    public Results match(final Map<String, String> headers) throws InterruptedException {
        final String userAgent = Provider.getUserAgent(headers);
        final Results exactResults = matchExact(userAgent, headers);
        if(exactResults != null) {
            return exactResults;
        }
        // There isn't a UA Prof match so use the handler specific methods.
        return match(userAgent);
    }

    /**
     *
     * Adds the device and it's user agent string to the collection of user
     * agent strings and devices. The User Agent string hashcode is the key of
     * the collection.
     *
     * @param device A new device to add.
     */
    private void setUserAgent(final BaseDeviceInfo device) {
        int hashcode = device.getUserAgent().hashCode();
        List<BaseDeviceInfo> value;
        // Does the hashcode already exist?
        if (_devices.containsKey(hashcode)) {
            value = _devices.get(hashcode);
            // Does the key already exist?
            for (int i = 0; i < value.size(); i++) {
                if (value.get(i).getUserAgent().equals(device.getUserAgent())) {
                    // Yes. Update with the new device and then exit.
                    value.add(i, device);
                    return;
                }
            }
            // No. Expand the array adding the new device.
            value.add(device);
            _devices.put(hashcode, value);
        } else {
            // Add the device to the collection.
            ArrayList<BaseDeviceInfo> d = new ArrayList<BaseDeviceInfo>();
            d.add(device);
            _devices.put(hashcode, d);
        }
    }

    /**
     *
     * Adds the device to the collection of devices with UA prof information. If
     * the device already exists the previous one is replaced.
     *
     * @param device Device to be added.
     */
    private void setUaProf(final BaseDeviceInfo device) {
        for (int index : _provider.getUserAgentProfileStringIndexes()) {
            final List<Integer> list = device.getPropertyValueStringIndexes(index);
            if (list != null) {
                for (int userAgentProfileStringIndex : list) {
                    final String value = _provider.getStrings().get(userAgentProfileStringIndex);

                    // Don't process empty values.
                    if (value == null) {
                        continue;
                    }
                    if (value.equals("")) {
                        continue;
                    }

                    // Get the hashcode and process
                    // the device and hashcode.
                    int hashcode = value.hashCode();
                    processUaProf(device, hashcode);
                    
                    // If the url is not valid don't continue processing.
                    String url = null;

                    // Add the domain to the list of domains for the handler.
                    Matcher m = _urlHost.matcher(value);
                    if (m.find()) {
                        url = m.group();
                    }

                    if (_uaProfDomains.contains(url) == false) {
                        _uaProfDomains.add(url);
                    }
                }
            }
        }
    }

    /**
     *
     * Processes the device and hashcode.
     *
     * @param device Device to be added.
     * @param hashcode hashcode value of the device.
     */
    private void processUaProf(BaseDeviceInfo device, int hashcode) {
        // Does the hashcode already exist?
        if (_uaprofs.containsKey(hashcode)) {
            // Does the key already exist?
            int index;
            for (index = 0; index < _uaprofs.get(hashcode).size(); index++) {
                if (!(_uaprofs.get(hashcode).get(index).getDeviceId().equals(device.getDeviceId()))) {
                    continue;
                }
                // Yes. Update with the new device and then exit.
                _uaprofs.get(hashcode).add(index, device);
                return;
            }
            // No. Expand the array adding the new device.
            ArrayList<BaseDeviceInfo> newList = new ArrayList<BaseDeviceInfo>(_uaprofs.get(hashcode));
            newList.add(device);
            _uaprofs.put(hashcode, newList);
        } else {
            // Add the device to the collection.
            ArrayList<BaseDeviceInfo> d = new ArrayList<BaseDeviceInfo>();
            d.add(device);
            _uaprofs.put(hashcode, d);
        }
    }

    /**
     *
     * Returns the devices that match a specific hashcode.
     *
     * @param dictionary Collection of hashcodes and devices.
     * @param value Value that's hashcode is being sought.
     * @return ArrayList of devices matching the value.
     */
    private static List<BaseDeviceInfo> getDeviceInfo(
            final Map<Integer, List<BaseDeviceInfo>> dictionary, 
            final String value) {
        if (dictionary != null && dictionary.containsKey(value.hashCode())) {
            return dictionary.get(value.hashCode());
        }
        return null;
    }
    
        /**
     *
     * Cleans User Agent Profile urls by removing quotes and commas.
     *
     * @param value The User Agent Profile url to clean.
     * @return The cleaned User Agent Profile url.
     */
    private static String cleanUserAgentProfileUrl(String value) {
        String[] values;
        if (value != null) {
            if (!(value.equals(""))) {
                values = value.split(",");
                values[0].replace("\"", "");
                return values[0];
            }
        }
        return value;
    }
}
