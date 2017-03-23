/*
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited.
 * Copyright © 2017 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
import fiftyone.mobile.detection.entities.Profile;
import fiftyone.mobile.detection.factories.StreamFactory;
import java.io.IOException;
import java.util.ArrayList;

/**
 * <!-- tutorial -->
 * Example shows how to use an existing 51Degrees deviceId to obtain a Match 
 * object with relevant properties and values.
 * <p>
 * Example illustrates:
 * <ul>
 *  <li>Loading a Provider from a Disk-based (Stream) Pattern Dataset
 *  <pre class="prettyprint lang-java">
 *  <code>
 *      provider = new Provider(StreamFactory.create
 *      (Shared.getLitePatternV32(), false));
 *  </code>
 *  </pre>
 *  <li>Matching a User-Agent string header value
 *  <pre class="prettyprint lang-java">
 *  <code>
 *      provider.match(mobileUserAgent);
 *  </code>
 *  </pre>
 *  <li>Using the match object to retrieve and store deviceId
 *  <pre class="prettyprint lang-java">
 *  <code>
 *      Match matchFromUA;
 *  </code>
 *  </pre>
 *  <p>as a string: 
 *  <pre class="prettyprint lang-java">
 *  <code>
 *      matchFromUA.getDeviceId();
 *  </code>
 *  </pre>
 *  <p>as byte array: 
 *  <pre class="prettyprint lang-java">
 *  <code>
 *      matchFromUA.getDeviceIdAsByteArray();
 *  </code>
 *  </pre>
 *  <p>as list of profile IDs: 
 *  <pre class="prettyprint lang-java">
 *  <code>
 *      for (Profile profile : matchFromUA.getProfiles()) {
 *  </code>
 *  </pre>
 *  <li>Creating a match object from deviceId
 *  <pre class="prettyprint lang-java">
 *  <code>
 *      device.matchForDeviceIdArray(deviceIdByteArray);
 *  </code>
 *  </pre>
 *  <pre class="prettyprint lang-java">
 *  <code>
 *      device.matchForDeviceIdString(deviceIdString);
 *  </code>
 *  </pre>
 *  <pre class="prettyprint lang-java">
 *  <code>
 *      device.matchForDeviceIdList(deviceIdList);
 *  </code>
 *  </pre>
 * </ul>
 * <p>
 * At the end a short console message will be printed containing a hash code of 
 * the object, deviceId and the value for the IsMobile property. Notice that 
 * all three objects are different as demonstrated by hash value but have the 
 * same deviceId and value for the IsMobile property.
 * <p>
 * Storing deviceId as opposed to the individual properties is a much more 
 * efficient way of retaining device information for future use. Byte array is 
 * the most efficient of the three options demonstrated as it only requires 
 * enough space to store the number of integers corresponding to the number of 
 * profiles (one profile per component).
 * <p>
 * <!-- tutorial -->
 * main assumes it is being run with a working directory at root of 
 * project or of this module.
 */
public class MatchForDeviceId {
    // Snippet Start
    // Device detection provider which takes User-Agents and returns matches.
    protected final Provider provider;
    
    // User-Agent string of a iPhone mobile device.
    protected final String mobileUserAgent = "Mozilla/5.0 (iPhone; CPU iPhone "
            + "OS 7_1 like Mac OS X) AppleWebKit/537.51.2 (KHTML, like Gecko) "
            + "Version/7.0 Mobile/11D167 Safari/9537.53";
    
    /**
     * Creates new provider object with the Stream dataset.
     * @throws IOException if there was a problem accessing the data file.
     */
    public MatchForDeviceId() throws IOException {
        provider = new Provider(StreamFactory.create(
                Shared.getLitePatternV32(), false));
    }
    
    /**
     * Performs a match of the pre-defined User-Agent to retrieve Id, then 
     * shows how to use the Id to get a Match object. The Id can be stored as a 
     * byte array, string or a list of integers corresponding to profiles.
     * <p>
     * Three match objects with different references but same deviceId and 
     * IsMobile values are generated to demonstrate that all three methods of 
     * storing deviceId produce the same result.
     * @param args command line arguments.
     * @throws IOException if there was a problem accessing data file.
     */
    public static void main(String[] args) throws IOException {
        MatchForDeviceId device = new MatchForDeviceId();
        // Get deviceId for some User-Agent.
        Match matchFromUA = device.matchForUserAgent(device.mobileUserAgent);
        // Store for future use.
        String deviceIdString = matchFromUA.getDeviceId();
        byte[] deviceIdByteArray = matchFromUA.getDeviceIdAsByteArray();
        ArrayList<Integer> deviceIdList = new ArrayList<Integer>();
        for (Profile profile : matchFromUA.getProfiles()) {
            deviceIdList.add(profile.profileId);
        }
        // Some time has passed. Get match for each of the storage methods.
        Match matchFromByteArray = device.matchForDeviceIdArray(deviceIdByteArray);
        Match matchFromIdString = device.matchForDeviceIdString(deviceIdString);
        Match matchFromIdList = device.matchForDeviceIdList(deviceIdList);
        
        System.out.println("Match object: " +
                System.identityHashCode(matchFromByteArray) + 
                " created from deviceId as byte array. " + 
                "deviceId: " + matchFromByteArray.getDeviceId() + 
                "IsMobile: " + matchFromByteArray.getValues("IsMobile"));
        
        System.out.println("Match object: " +
                System.identityHashCode(matchFromIdString) + 
                " created from deviceId as string. " + 
                "deviceId: " + matchFromIdString.getDeviceId() + 
                " IsMobile: " + matchFromIdString.getValues("IsMobile"));
        
        System.out.println("Match object: " +
                System.identityHashCode(matchFromIdList) + 
                " created from deviceId as list of profile Ids. " + 
                "deviceId: " + matchFromIdList.getDeviceId() + 
                "IsMobile: " + matchFromIdList.getValues("IsMobile"));
    }
    
    /**
     * Performs match for a User-Agent string and returns Match object with 
     * detection results.
     * 
     * @param userAgent String representing typical HTTP User-Agent header.
     * @return Match object with detection results.
     * @throws IOException if there was a problem accessing the data file.
     */
    public Match matchForUserAgent(String userAgent) throws IOException {
        return provider.match(mobileUserAgent);
    }
    
    /**
     * Returns a Match object corresponding to the provided string deviceId.
     * String deviceId derived by: <code>match.getDeviceId();</code>
     * 
     * @param deviceId String representation of the deviceId.
     * @return Match object with detection results.
     * @throws IOException if there was a problem accessing the data file.
     */
    public Match matchForDeviceIdString(String deviceId) throws IOException {
        return provider.matchForDeviceId(deviceId);
    }
    
    /**
     * Returns a Match object corresponding to the provided byte array Id. 
     * Byte array id is retrieved by: 
     * <code>match.getDeviceIdAsByteArray();</code>
     * 
     * @param deviceId byte array representation of deviceId.
     * @return Match object with detection results.
     * @throws IOException if there was a problem accessing the data file.
     */
    public Match matchForDeviceIdArray(byte[] deviceId) throws IOException {
        return provider.matchForDeviceId(deviceId);
    }
    
    /**
     * Returns a Match object corresponding to the provided list of profile IDs.
     * A list of profile IDs is derived from: 
     * <code>for (Profile profile : match.getProfiles()) {</code>
     * 
     * @param deviceIds a list of integers where each integer is a profile Id.
     * @return Match object with detection results.
     * @throws IOException if there was a problem accessing the data file.
     */
    public Match matchForDeviceIdList(ArrayList<Integer> deviceIds) 
                                                            throws IOException {
        return provider.matchForDeviceId(deviceIds);
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
    public void close() throws IOException {
        provider.dataSet.close();
    }
    // Snippet End
}
