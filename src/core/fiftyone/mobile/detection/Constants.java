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

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A class that holds constant values that will be used throughout the code.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class Constants {
    
    /**
     * String to hold the path to the data file.
     */
    public static final String DATA_FILE_NAME = "51Degrees.mobi-Premium.dat";    
    /**
     * Holds the path to the .dat file. (even though it says .dat, it is
     * .dat.gz)
     */
    public static final String EMBEDDED_RESOURCE_PATH = "resources/51Degrees.mobi-Lite.dat";
    /**
     * ENUM used to identify handler.
     */
    public enum HandlerTypes {

        UNKNOWN, EDITDISTANCE, REGEXSEGMENT, REDUCEDINITIALSTRING;

        public static HandlerTypes type(final int handlerId) {
            switch (handlerId) {
                case 1:
                    return EDITDISTANCE;
                case 2:
                    return REGEXSEGMENT;
                case 3:
                    return REDUCEDINITIALSTRING;
                default:
                    return UNKNOWN;
            }
        }
    }
    /**
     * Holds the current major and minor version numbers.
     */
    public static final int FORMAT_VRSION_MAJOR = 1;
    public static final int FORMAT_VERSION_MINOR = 0;
    /**
     * Highest integer value
     */
    public static final int MAX_INT = Integer.MAX_VALUE;
    /**
     * The character used to separate property values when concatenated as a
     * single String.
     */
    public static final String VALUE_SEPERATOR = "|";
    /**
     * The character used to seperate profile integer values in the device id.
     */
    public static final String PROFILE_SEPERATOR = "-";
    /**
     * The name of the unique property key used to return the device id.
     */
    public static final String DEVICE_ID = "Id";
    /**
     * Improves performance of segment handlers by storing the results of User
     * Agent segment matches to improve performance at the expense of memory
     * consumption. Set this to false to reduce memory consumption.
     */
    public static final boolean STORE_SEGMENT_RESULTS = true;
    /**
     * Array of transcoder HTTP headers that represent the User Agent String of
     * the mobile device rather than the desktop browser.
     */
    public static final String[] DEVICE_USER_AGENT_HEADERS = new String[]{ 
        "device-stock-ua",
        "x-device-user-agent",
        "x-device-user-agent",
        "x-operamini-phone-ua"
    };
    /**
     * The Http header field that contains the User Agent.
     */
    public static final String USER_AGENT_HEADER = "user-agent";
    /**
     * The name of the property which contains the user agent profile.
     */
    public static final String[] USER_AGENT_PROFILES = new String[]{"UserAgentProfile"};
    /**
     * A list of properties to exclude from the AllProperties results.
     */
    public static final List<String> EXCLUDE_PROPERTIES_FROM_ALL_PROPERTIES = Arrays.asList("");
    /**
     * URL used to retrieve the latest premium data.
     */
    public static final String AUTO_UPDATE_URL = "https://51degrees.mobi/Products/Downloads/Premium.aspx";
    /**
     * Regular expression used to validate License Keys.
     */
    public static final Pattern LICENSE_KEY_VALIDATION_REGEX = Pattern.compile("^[A-Z\\d]+$");
    /**
     * The length of time to wait before starting the auto update process. Set
     * to zero to disable auto update.
     */
    public static final long AUTO_UPDATE_DELAYED_START = 20 * 1000;
    /**
     * The length of time to wait before checking for a newer version of the
     * device data file.
     */
    public static final long AUTO_UPDATE_WAIT = 6 * 24 * 60 * 60 * 1000;
    /**
     * The length of time to sleep before checking for new device data again.
     */
    public static final long AUT0_UPDATE_SLEEP = 6 * 60 * 60 * 1000;    
    
    /**
     * The length of time to wait before starting the local data file check
     * process.
     */
    public static final long FILE_CHECK_DELAYED_START = 20 * 60 * 1000;
    
    /**
     * The length of time to sleep in between local file checks.
     */
    public static final long FILE_CHECK_SLEEP = 20 * 60 * 1000;
    
    /**
     * The version of the code. Note that this refers to the API,
     * not the data.
     */
    public static final String VERSION = "2.2.9.1";
    
    /**
     * The amount of free memory, in bytes, that the JVM must have
     * available before performing a data update.
     */
    public static final long AUTO_UPDATE_REQUIRED_FREE_MEMORY = 100 * 1024 * 1024;
}
