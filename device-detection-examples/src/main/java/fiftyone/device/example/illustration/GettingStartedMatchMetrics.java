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
 *  <!-- snippet -->
 * Getting started example of using 51Degrees device detection match metrics 
 * information. The example shows how to;
 * <ol>
 *  <li>instantiate 51Degrees detection provider
 *  <p><code>provider = new Provider(StreamFactory.create(
 *  Shared.getLitePatternV32(), false));</code>
 *  <li>pass in a single HTTP User-Agent header
 *  <p><code>Match match = provider.match(userAgent);</code>
 *  <li>obtain device Id: consists of four components separated by a hyphen 
 *  symbol: Hardware-Platform-Browser-IsCrawler where each Component is 
 *  represented an ID of the corresponding Profile.
 *  <p><code>match.getDeviceId();</code>
 *  <li>obtain match method: provides information about the 
 *  algorithm that was used to perform detection for a particular User-Agent. 
 *  For more information on what each method means please see: 
 *  <a href="https://51degrees.com/support/documentation/pattern">
 *  How device detection works</a>
 *  <p><code>match.getMethod();</code>
 *  <li>obtain difference:  used when detection method is not Exact or None. 
 *  This is an integer value and the larger the value the less confident the 
 *  detector is in this result.
 *  <p><code>match.getDifference();</code>
 *  <li>obtain signature rank: an integer value that indicates how popular 
 *  the device is. The lower the rank the more popular the signature.
 *  <p><code>match.getSignature().getRank();</code>
 * </ol>
 * <p>
 * {@link #main} assumes it is being run with a working directory at root of 
 * project or of this module.
 * <!-- snippet -->
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
     * file. For more data see: 
     * <a href="https://51degrees.com/compare-data-options">compare data options
     * </a>
     * 
     * @throws IOException can be thrown if there is a problem reading from the 
     * provided data file.
     */
    public GettingStartedMatchMetrics() throws IOException {
        provider = new Provider(StreamFactory.create(
                Shared.getLitePatternV32(), true));
    }
    
    /**
     * Extracts MatchMethods enumeration value from the {@link Match} object.
     * The extracted value represents the algorithm used to perform this
     * detection.
     * <p>
     * {@code Match} object must be created and populated by invoking the 
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
     * Extract the difference value from the {@link Match} object provided.
     * Difference represents the level of confidence the detector has in the 
     * accuracy of the current detection. The higher the number the less 
     * confident the detector is.
     * <p>
     * Difference value is only relevant to Nearest, Closest and Numeric 
     * methods. Exact method will always have a value of zero. Difference is 
     * irrelevant to None as the User-Agent that yielded this result is almost 
     * certainly fake.
     * <p>
     * {@code Match} object must be created and populated by invoking the 
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
     * Extracts the signature rank from the {@link Match} object provided.
     * Signature rank indicates the relative level of popularity of the given 
     * signature. The lower the value the more popular the requesting device is.
     * <p>
     * Popularity is determined by 51Degrees and is based on our statistics.
     * <p>
     * {@code Match} object must be created and populated by invoking the 
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
     * <li>performs detection; 
     * <li>stores detection information in a {@link Match} object;
     * <li>Each {@code Match} object is then passed to one of the relevant 
     * methods to retrieve match metrics information, i.e.: 
     * {@link #getDeviceId} is invoked to return an Id string.
     * </ol>
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
