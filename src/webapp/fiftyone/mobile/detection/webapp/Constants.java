package fiftyone.mobile.detection.webapp;

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

public class Constants {

    /**
     * The version of the API to report in usage sharing.
     */
    public static final String VERSION = "3.1.8.4";
    
    /**
     * Context parameter in the web.xml file for the file path.
     */
    public static final String BINARY_FILE_PATH = "BINARY_FILE_PATH";
    
    /**
     * Context parameter in the web.xml file for the mode used to load
     * the data file.
     */
    public static final String MEMORY_MODE = "MEMORY_MODE";
    
    /**
     * Context parameter in the web.xml file for controlling usage sharing.
     */
    public static final String SHARE_USAGE = "SHARE_USAGE";
    
    /**
     * Url used to share usage information with 51Degrees.
     */
    public static final String URL = "http://devices.51degrees.mobi/new.ashx";

    /**
     * Number of new device requests before the share usage information is
     * sent to 51Degrees.
     */
    public static final int NEW_DEVICE_QUEUE_LENGTH = 50;
    
    /**
     * Timeout in milliseconds before usage sharing is abandoned.
     */
    public static final int NEW_URL_TIMEOUT = 10000;  
    
    public static final String IMAGE_MAX_WIDTH = "IMAGE_MAX_WIDTH";
    
    public static final String IMAGE_MAX_HEIGHT = "IMAGE_MAX_HEIGHT";
    
    public static final String IMAGE_FACTOR = "IMAGE_FACTOR";
    
    public static final String DEFAULT_AUTO = "IMAGE_DEFAULT_AUTO";
    
    public static final String IMAGE_WIDTH_PARAM = "IMAGE_WIDTH_PARAM";
    
    public static final String IMAGE_HEIGHT_PARAM = "IMAGE_HEIGHT_PARAM";
    
    /**
     * The number of seconds to wait between checks for new data files at 
     * 51Degrees.
     */
    public static final int AUTO_UPDATE_WAIT = 30 * 60;
    
    /**
     * The number of seconds to wait before performing the first check for
     * a new data file at 51Degrees.
     */
    public static final int AUTO_UPDATE_DELAYED_START = 10;
    
    /**
     * The number of seconds to wait between checks for new data files on the
     * disk. Another process may have performed the
     * update.
     */
    public static final int FILE_CHECK_WAIT = 2 * 60;
    
    /**
     * The number of seconds to wait before performing the first check for
     * a new data file on the disk. Another process may have performed the
     * update.
     */
    public static final int FILE_CHECK_DELAYED_START = 5 * 60;
    
    /**
     * Number of seconds between the cache being serviced.
     */
    public static final int CACHE_SERVICE_INTERVAL = 60;

    /**
     * Header fields that should not be shared with 51Degrees.
     */
    public static final String[] IGNORE_HEADER_FIELD_VALUES = new String[]{
        "Referer",
        "cookie",
        "AspFilterSessionId",
        "Akamai-Origin-Hop",
        "Cache-Control",
        "Cneonction",
        "Connection",
        "Content-Filter-Helper",
        "Content-Length",
        "Cookie",
        "Cookie2",
        "Date",
        "Etag",
        "If-Last-Modified",
        "If-Match",
        "If-Modified-Since",
        "If-None-Match",
        "If-Range",
        "If-Unmodified-Since",
        "IMof-dified-Since",
        "INof-ne-Match",
        "Keep-Alive",
        "Max-Forwards",
        "mmd5",
        "nnCoection",
        "Origin",
        "ORIGINAL-REQUEST",
        "Original-Url",
        "Pragma",
        "Proxy-Connection",
        "Range",
        "Referrer",
        "Script-Url",
        "Unless-Modified-Since",
        "URL",
        "UrlID",
        "URLSCAN-ORIGINAL-URL",
        "UVISS-Referer",
        "X-ARR-LOG-ID",
        "X-Cachebuster",
        "X-Discard",
        "X-dotDefender-first-line",
        "X-DRUTT-REQUEST-ID",
        "X-Initial-Url",
        "X-Original-URL",
        "X-PageView",
        "X-REQUEST-URI",
        "X-REWRITE-URL",
        "x-tag",
        "x-up-subno",
        "X-Varnish"};
}