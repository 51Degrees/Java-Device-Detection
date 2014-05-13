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
package fiftyone.mobile.detection.webapp;

import fiftyone.mobile.detection.NewDeviceDetails;

/**
 * A class that holds constant values that will be used in the webapp.
 *
 * @author 51Degrees.mobi
 * @version 2.2.8.7
 */
public class Constants {
    /**
    * The default path to use to load the data file.
    */
    public static final String DEFAULT_DATA_FILE_PATH = 
            "WEB-INF/" + fiftyone.mobile.detection.Constants.DATA_FILE_NAME;
    /**
     * The key in the servlet contexts attributes collection to return this
     * instance of the factory.
     */
    public static final String FACTORY_KEY = "51D_LISTENER";    
    /**
    * Indicates if usage data should be shared with 51Degrees.mobi. We
    * recommended leaving this value unchanged to ensure we're improving
    * the performance and accuracy of the solution.
    */
    public static final boolean SHARE_USAGE = true;
    /**
     * URL to send new device data to.
     */
    public static final String NEW_DEVICE_URL ="http://devices.51degrees.mobi/new.ashx";
    /**
     * The detail that should be provided relating to new devices.
     */
    public static final NewDeviceDetails NEW_DEVICE_DETAIL = NewDeviceDetails.Maximum;
    /**
     * Length of the queue in new device length.
     */
    public static final int NEW_DEVICE_QUEUE_LENGTH = 50;
    /**
     * Timeout on a URL in share usage.
     */
    public static final int NEW_URL_TIMEOUT = 10000;  
    /**
     * The content of fields in this array should not be included in the request
     * information sent to 51degrees.
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
    
    /**
     * The version of the code. Note that this refers to the API,
     * not the data.
     */
    public static final String VERSION = "2.2.8.7";
}