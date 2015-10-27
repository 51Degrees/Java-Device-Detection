/*
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited.
 * Copyright © 2015 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
 */

package fiftyone.device.example.illustration;

import fiftyone.device.example.Shared;
import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.factories.StreamFactory;
import java.io.Closeable;
import java.io.IOException;

/**
 * Getting started example of using 51Degrees device detection. The example 
 * shows how to;
 * 
 * 1) instantiate 51Degrees detection provider;
 * 2) pass in a single HTTP User-Agent header; and 
 * 3) extract the IsMobile value.
 */
public class GettingStarted implements Closeable {
    
    // Device detection provider which takes User-Agents and returns matches.
    protected final Provider provider;
 
    // User-Agent string of a iPhone mobile device.
    protected final String mobileUserAgent = "Mozilla/5.0 (iPhone; CPU iPhone "
            + "OS 7_1 like Mac OS X) AppleWebKit/537.51.2 (KHTML, like Gecko) "
            + "Version/7.0 Mobile/11D167 Safari/9537.53";
    
    // User-Agent string of Firefox Web browser of version 41 used on desktop.
    protected final String desktopUserAgent = "Mozilla/5.0 (Windows NT 6.3; "
            + "WOW64; rv:41.0) Gecko/20100101 Firefox/41.0";
    
    // User-Agent string of a MediaHub device.
    protected final String mediaHubUserAgent = "Mozilla/5.0 (Linux; Android 4.4"
            + ".2; X7 Quad Core Build/KOT49H) AppleWebKit/537.36 (KHTML, like "
            + "Gecko) Version/4.0 Chrome/30.0.0.0 Safari/537.36";
    
    /**
     * Initialises the device detection Provider with the included Lite data
     * file. For more data see - https://51degrees.com/compare-data-options
     * 
     * @throws IOException can be thrown if there is a problem reading from the 
     * provided data file.
     */
    public GettingStarted() throws IOException {
        provider = new Provider(StreamFactory.create(
                Shared.getLitePatternV32(), false));
    }
    
    /**
     * Method performs device detection by invoking the match method of the 
     * provider object. Match results are then stored in the Match object.
     * 
     * @param userAgent HTTP User-Agent string.
     * @return String with value for the IsMobile property for a given User-Agent.
     * @throws IOException if there is a problem accessing the data file.
     */
    public String detect(String userAgent) throws IOException {
        Match match = provider.match(userAgent);
        return match.getValues("IsMobile").toString();
    }
    
    /**
     * Closes the Dataset by releasing data file readers and freeing the data 
     * file from locking.
     * 
     * @throws IOException if there is a problem accessing the data file.
     */
    @Override
    public void close() throws IOException {
        provider.dataSet.close();
    }   
           
    /**
     * Main entry point that instantiates the GettingStarted class, invokes 
     * detect method, prints results and finally closes the Dataset.
     * 
     * @param args command line arguments, not used.
     * @throws java.io.IOException if there is a problem accessing the data file 
     * that will be used for device detection.
     */
    public static void main(String[] args) throws IOException {
        System.out.println("Starting GettingStarted example.");
        GettingStarted gs = new GettingStarted();
        try {
            System.out.println("Mobile User-Agent: " + gs.mobileUserAgent);
            System.out.println("IsMobile: " + gs.detect(gs.mobileUserAgent));
            System.out.println("Desktop User-Agent: " + gs.desktopUserAgent);
            System.out.println("IsMobile: " + gs.detect(gs.desktopUserAgent));
            System.out.println("MediaHub User-Agent: " + gs.mediaHubUserAgent);
            System.out.println("IsMobile: " + gs.detect(gs.mediaHubUserAgent));
        } finally {
            gs.close();
        }
    }
}
