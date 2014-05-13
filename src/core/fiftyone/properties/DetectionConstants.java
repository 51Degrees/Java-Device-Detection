package fiftyone.properties;

import java.util.regex.Pattern;

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
 * Constants used by the detection routines. *
 */
public class DetectionConstants {

    /**
     * Character to use when combining values from properties that support lists
     * and may have more than one value. For example; SupportedBearers.
     */
    public static final String VALUE_SEPARATOR = "|";
    /**
     * Length of time in seconds to wait between cache services when operating
     * in stream mode.
     */
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
     * Resource name of the embedded data file.
     */
    public static final String EMBEDDED_DATA_RESOURCE_NAME = "51Degrees-Lite.dat";
    public static final String AUTO_UPDATE_URL = "https://51degrees.mobi/Products/Downloads/Premium.aspx";
    /**
     * The amount of free memory, in bytes, that the JVM must have available
     * before performing a data update.
     */
    public static final long AUTO_UPDATE_REQUIRED_FREE_MEMORY = 100 * 1024 * 1024;
    
    /**
     * Regular expression used to validate License Keys.
     */
    public static final Pattern LICENSE_KEY_VALIDATION_REGEX = Pattern.compile("^[A-Z\\d]+$");

	/**
	 * The major version of the API.
	 */
	public static final int MAJOR_VERSION = 3;

	/**
	 * The minor version of the API.
	 */
	public static final int MINOR_VERSION = 0;
}
