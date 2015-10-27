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
import fiftyone.properties.MatchMethods;
import java.io.Closeable;
import java.io.IOException;

/**
 * Getting started example of using 51Degrees device detection match metrics 
 * information. The example shows how to;
 * 
 * 1) instantiate 51Degrees detection provider;
 * 2) pass in a single HTTP User-Agent header; and 
 * 3) obtain device Id, match method, difference and signature rank.
 * 
 * DeviceId: is consists of four components separated by a hyphen symbol:
 * Hardware-Platform-Browser-IsCrawler where each component is in turn an ID of 
 * the corresponding Profile.
 * Match method: provides information about the algorithm that was used to 
 * perform detection for a particular User-Agent. For more information on what 
 * each method means please see: 
 * https://51degrees.com/support/documentation/pattern
 * Difference: is used when detection method is not Exact or None. This is an 
 * integer value and the larger the value the less confident the detector is in 
 * this result.
 * Rank: integer value that indicates how popular the device is. The lower the 
 * rank the more popular the signature.
 */
public class GettingStartedMatchMetrics implements Closeable {
    
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
     * file. For more data see - https://51degrees.com/compare-data-options
     * 
     * @throws IOException can be thrown if there is a problem reading from the 
     * provided data file.
     */
    public GettingStartedMatchMetrics() throws IOException {
        provider = new Provider(StreamFactory.create(
                Shared.getLitePatternV32(), true));
    }
    
    /**
     * Method extracts MatchMethods enumeration value from the Match object.
     * The extracted value represents what algorithm was used to perform this
     * detection.
     * 
     * Match object must be created and populated by invoking the 
     * <code>provider.match(userAgentString)</code> before supplying to this 
     * method.
     * 
     * @param match Match object containing detection results, not null.
     * @return MatchMethods enumeration used for this detection.
     */
    public MatchMethods getMethod(Match match) {
        if (match == null) {
            throw new IllegalArgumentException();
        }
        return match.getMethod();
    }
    
    /**
     * Method extract the difference value from the Match object provided.
     * Difference represents the level of confidence the detector has in the 
     * accuracy of the current detection. The higher the number the less 
     * confident the detector is.
     * 
     * Difference value is only relevant to Nearest, Closest and Numeric 
     * methods. Exact method will always have a value of zero. Difference is 
     * irrelevant to None as the User-Agent that yielded this result is almost 
     * certainly fake.
     * 
     * Match object must be created and populated by invoking the 
     * <code>provider.match(userAgentString)</code> before supplying to this 
     * method.
     * 
     * @param match Match object containing detection results, not null.
     * @return integer value of difference indicating the level of confidence 
     * the detector has in the current match results.
     */
    public int getDifference(Match match) {
        if (match == null) {
            throw new IllegalArgumentException();
        }
        return match.getDifference();
    }
    
    /**
     * Method extracts the signature rank from the Match object provided.
     * Signature rank indicates the relative level of popularity of the given 
     * signature. The lower the value the more popular the requesting device is.
     * 
     * Popularity is determined by 51Degrees and is based on our statistics.
     * 
     * Match object must be created and populated by invoking the 
     * <code>provider.match(userAgentString)</code> before supplying to this 
     * method.
     * 
     * @param match Match object containing detection results, not null.
     * @return integer representing the popularity of the matched device. The 
     * lower the number the more popular device is.
     * @throws IOException if there is a problem accessing the data file.
     */
    public int getRank(Match match) throws IOException {
        if (match == null) {
            throw new IllegalArgumentException();
        }
        return match.getSignature().getRank();
    }
    
    /**
     * Device ID is a string of four numeric components separated by hyphen.
     * A relevant profile is picked by the detector for each of the following 
     * components: Hardware-Platform-Browser-IsCrawler.
     * 
     * @param match Match object containing detection results, not null.
     * @return String with device ID consisting of four profile IDs.
     * @throws IOException if there is a problem accessing the data file.
     */
    public String getDeviceId(Match match) throws IOException {
        if (match == null) {
            throw new IllegalArgumentException();
        }
        return match.getDeviceId();
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
     * Main entry point that instantiates GettingStartedMatchMetrics class. 
     * For each of the User-Agents defined in this class method invokes 
     * detection and stores detection information in a Match object. Match 
     * object is then passed to one of the relevant methods to retrieve the 
     * required result, i.e.: getDeviceId is invoked to return an Id string.
     * 
     * @param args command line arguments, not used.
     * @throws IOException if there is a problem accessing the data file. 
     */
    public static void main(String[] args) throws IOException {
        GettingStartedMatchMetrics gs = new GettingStartedMatchMetrics();
        Match match;
        try {
            // Display metrics for mobile User-Agent.
            match = gs.provider.match(gs.mobileUserAgent);
            System.out.println("User-Agent: "+gs.mobileUserAgent);
            System.out.println("Id: "+gs.getDeviceId(match));
            System.out.println("Detection method: "+gs.getMethod(match));
            System.out.println("Difference: "+gs.getDifference(match));
            System.out.println("Rank: "+gs.getRank(match));
            // Display metrics for desktop User-Agent.
            match = gs.provider.match(gs.desktopUserAgent);
            System.out.println("User-Agent: "+gs.desktopUserAgent);
            System.out.println("Id: "+gs.getDeviceId(match));
            System.out.println("Detection method: "+gs.getMethod(match));
            System.out.println("Difference: "+gs.getDifference(match));
            System.out.println("Rank: "+gs.getRank(match));
            // Display metrics for mediahub User-Agent.
            match = gs.provider.match(gs.mediaHubUserAgent);
            System.out.println("User-Agent: "+gs.mediaHubUserAgent);
            System.out.println("Id: "+gs.getDeviceId(match));
            System.out.println("Detection method: "+gs.getMethod(match));
            System.out.println("Difference: "+gs.getDifference(match));
            System.out.println("Rank: "+gs.getRank(match));
        } finally {
            gs.close();
        }
    }
}
