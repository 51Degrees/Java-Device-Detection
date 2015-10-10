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
package fiftyone.properties;

import fiftyone.mobile.detection.entities.Version;
import java.util.regex.Pattern;

/**
 * Constants used by the detection routines. *
 */
public class DetectionConstants {

    public enum FORMAT_VERSIONS {
        /**
         * First released in May 2014 for version 3 device deteciton.
         */
        PatternV31,
        /**
         * Contains the same data as V3.1 but organises the information more 
         * efficiently to reduce data file size and improve performance.
         */
        PatternV32,
        /**
         * The binary data file format used with Trie version 3 device
         * detection.
         */
        TrieV30
    }
    
    /**
     * 
     */
    public static final SupportedPatternFormatVersions 
            supportedPatternFormatVersions = new SupportedPatternFormatVersions();
    
    /**
     * The format version of the binary data contained in the file header. This
     * much match with the data file for the file to be read.
     * Deprecated since this release supports both 3.1 and 3.2.
     */
    @Deprecated
    public static final Version FormatVersion = new Version(3, 1, 0, 0);
    /**
     * Character to use when combining values from properties that support lists
     * and may have more than one value. For example; SupportedBearers.
     */
    public static final String VALUE_SEPARATOR = "|";
    /**
     * Deprecated since V3.2 no longer relies on a Timer for cache service.
     * Length of time in seconds to wait between cache services when operating
     * in stream mode.
     */
    @Deprecated
    public static final int CACHE_SERVICE_INTERVAL = 60;
    /**
     * Separator used to combine profile Ids to form device Ids.
     */
    public static final String PROFILE_SEPARATOR = "-";
    /**
     * The key in the dictionary of results used to provide the confidence
     * integer value.
     */
    public static final String DIFFERENCE_PROPERTY = "Difference";
    /**
     * The key in the dictionary of results used to provide the nodes found as a
     * combined string.
     */
    public static final String NODES = "Nodes";
    /**
     * The key in the dictionary of results used to provide the id of the device
     * as a string.
     */
    public static final String DEVICEID = "Id";
    /**
     * Deprecated since V3.2 no longer uses the embedded data file.
     * Resource name of the embedded data file.
     */
    @Deprecated
    public static final String EMBEDDED_DATA_RESOURCE_NAME = "51Degrees-Lite.dat";
    /**
     * URL for the service that provides automatic updates of device data.
     */
    public static final String AUTO_UPDATE_URL = "https://51degrees.com/Products/Downloads/Premium.aspx";
    /**
     * Deprecated since V3.2 uses a temporary data file instead of memory to 
     * download the update.
     * The amount of free memory, in bytes, that the JVM must have available
     * before performing a data update.
     */
    @Deprecated
    public static final long AUTO_UPDATE_REQUIRED_FREE_MEMORY = 100 * 1024 * 1024;
    /**
     * Regular expression used to validate License Keys.
     */
    public static final Pattern LICENSE_KEY_VALIDATION_REGEX = Pattern.compile("^[A-Z\\d]+$");
    /**
     * HTTP header for the user agent.
     */
    public static final String USER_AGENT_HEADER = "User-Agent";
    /**
     * Array of HTTP headers that represent the useragent string of the device
     * rather than the browser.
     */
    public static final String[] DEVICE_USER_AGENT_HEADERS = new String[]{
        "User-Agent",
        "Device-Stock-UA",
        "x-Device-User-Agent",
        "X-Device-User-Agent",
        "X-OperaMini-Phone-UA"
    };
    /**
     * Number of cache entries for dataset strings.
     */
    public static final int STRINGS_CACHE_SIZE = 3000;
    /**
     * Number of cache entries for dataset values.
     */
    public static final int VALUES_CACHE_SIZE = 3000;
    /**
     * Number of cache entries for dataset profiles.
     */
    public static final int PROFILE_CACHE_SIZE = 300;
    /**
     * Number of cache entries for dataset signatures.
     */
    public static final int SIGNATURES_CACHE_SIZE = 16000;
    /**
     * Number of cache entries for dataset ranked signatures.
     */
    public static final int RANKED_SIGNATURE_CACHE_SIZE = 16000;
    /**
     * Number of cache entries for dataset nodes.
     */
    public static final int NODES_CACHE_SIZE = 30000;
    /**
     * Sizes of variables.
     */
    public static final int SIZE_OF_LONG = 8,
            SIZE_OF_UINT = 4,
            SIZE_OF_INT = 4,
            SIZE_OF_USHORT = 2,
            SIZE_OF_SHORT = 2,
            SIZE_OF_UBYTE = 1,
            SIZE_OF_BYTE = 1,
            SIZE_OF_BOOL = 1;
}
