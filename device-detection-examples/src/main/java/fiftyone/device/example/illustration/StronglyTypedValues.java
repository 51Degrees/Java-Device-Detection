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
 * <!-- snippet -->
 * Getting started example of using 51Degrees device detection. The example 
 * shows how to:
 * 
 * <ol>
 *  <li>Instantiate 51Degrees detection provider
 *  <p><code>provider = new Provider(StreamFactory.create(
 *  Shared.getLitePatternV32(), false));</code>
 *  <li>Pass in a single HTTP User-Agent header
 *  <p><code>Match match = provider.match(userAgent);</code>
 *  <li>Extract the value of the IsMobile property as boolean
 *  <p><code>match.getValues("IsMobile").toBool();</code>
 * </ol>
 * 
 * {@link #main} assumes it is being run with a working directory at root of 
 * project or of this module.
 * <!-- snippet -->
 */
public class StronglyTypedValues implements Closeable {
    
    // Device detection provider which takes User-Agents and returns matches.
    protected final Provider provider;
    
    // User-Agent string of a iPhone mobile device.
    protected final String mobileUserAgent = "Mozilla/5.0 (iPhone; CPU iPhone "
            + "OS 7_1 like Mac OS X) AppleWebKit/537.51.2 (KHTML, like Gecko) "
            + "Version/7.0 Mobile/11D167 Safari/9537.53";
    
    // User-Agent string of Firefox Web browser of version 41 used on desktop.
    protected final String desktopUserAgent = "Mozilla/5.0 (Windows NT 6.3; " +
            "WOW64; rv:41.0) Gecko/20100101 Firefox/41.0";
    
    // User-Agent string of a MediaHub device.
    protected final String mediaHubUserAgent = "Mozilla/5.0 (Linux; Android 4.4"
            + ".2; X7 Quad Core Build/KOT49H) AppleWebKit/537.36 (KHTML, like "
            + "Gecko) Version/4.0 Chrome/30.0.0.0 Safari/537.36";
    
    /**
     * Initialises the device detection Provider with the included Lite data
     * file. For more data see: 
     * <a href="https://51degrees.com/compare-data-options">compare data options
     * </a>
     * 
     * @throws IOException can be thrown if there is a problem reading from the 
     * provided data file.
     */
    public StronglyTypedValues() throws IOException {
        provider = new Provider(StreamFactory.create(
                Shared.getLitePatternV32(), false));
    }

    /**
     * Matches provided User-Agent string and returns the IsMobile property 
     * value as boolean.
     * Detection initiated by invoking {@link Provider#match(java.lang.String)}.
     * Detection results are then stored in the {@link Match} object and can be 
     * accessed using the {@code Match.getValues("PropertyName")} method.
     * 
     * @param userAgent HTTP User-Agent string.
     * @return True if the User-Agent is mobile, False otherwise.
     * @throws IOException if there is a problem accessing the data file. 
     */
    public boolean isMobile(String userAgent) throws IOException {
        Match match = provider.match(userAgent);
        return match.getValues("IsMobile").toBool();
    }
    
    /**
     * Closes the {@link fiftyone.mobile.detection.Dataset} by releasing data 
     * file readers and freeing the data file from locks. This method should 
     * only be used when the {@code Dataset} is no longer required, i.e. when 
     * device detection functionality is no longer required, or the data file 
     * needs to be freed.
     * 
     * @throws IOException if there is a problem accessing the data file.
     */
    @Override
    public void close() throws IOException {
        provider.dataSet.close();
    }
    
    /**
     * Main entry point for this example. For each of the User-Agents defined 
     * in this class: 
     * <ol> 
     * <li>invokes {@link #isMobile(java.lang.String) } method; and
     * <li>prints output depending on the boolean value returned by the 
     * {@code isMobile} method.
     * </ol>
     * 
     * @param args command line arguments, not used.
     * @throws IOException if there is a problem accessing the data file. 
     */
     public static void main(String[] args) throws IOException {
        System.out.println("Starting GettingStartedStronglyTyped example.");
        StronglyTypedValues gs = new StronglyTypedValues();
        try {
            System.out.println("User-Agent: "+gs.mobileUserAgent);
            if(gs.isMobile(gs.mobileUserAgent)) {
                System.out.println("Mobile");
            } else {
                System.out.println("Non-Mobile");
            }
            System.out.println("User-Agent: "+gs.desktopUserAgent);
            if(gs.isMobile(gs.desktopUserAgent)) {
                System.out.println("Mobile");
            } else {
                System.out.println("Non-Mobile");
            }
            System.out.println("User-Agent: "+gs.mediaHubUserAgent);
            if(gs.isMobile(gs.mediaHubUserAgent)) {
                System.out.println("Mobile");
            } else {
                System.out.println("Non-Mobile");
            }
        } finally {
            gs.close();
        }
    }
}
