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

package fiftyone.mobile.detection.test.type.api;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.DetectionTestSupport;
import fiftyone.mobile.detection.entities.Component;
import fiftyone.mobile.detection.entities.Profile;
import fiftyone.mobile.detection.entities.Values;
import fiftyone.mobile.detection.test.TestType;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static fiftyone.mobile.detection.test.common.UserAgentGenerator.getRandomUserAgent;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Superclass containing API pattern tests, subclassed for each type of Pattern file
 * <p>
 * Beware: The class level {@link Category} annotation does not seem to work
 * without one of the test methods being annotated
 */
@Category(TestType.TypeApi.class)
public abstract class ApiBase extends DetectionTestSupport {

    public abstract Provider getProvider();
    public abstract Dataset getDataset();
    
    // NB Do not remove the @Category annotation it seems to be needed to trigger the
    // correct operation of the class level annotation
    @Test
    @Category(TestType.TypeApi.class)
    public void allHeaders() throws IOException {
        Map<String, String> headers = new HashMap<String, String>();
        for (String header : getDataset().getHttpHeaders()) {
            headers.put(header, getRandomUserAgent(0));
        }
        fetchAllProperties(getProvider().match(headers));
    }
    
    @Test
    public void allHeadersNull() throws IOException {
        Map<String, String> headers = new HashMap<String, String>();
        for (String header : getDataset().getHttpHeaders()) {
            headers.put(header, null);
        }
        fetchAllProperties(getProvider().match(headers));
    }
    
    @Test
    public void duplicateHeaders() throws IOException {
        Map<String, String> headers = new HashMap<String, String>();
        for (int i = 0; i < 5; i++) {
            for (String header : getDataset().getHttpHeaders()) {
                headers.put(header, getRandomUserAgent(0));
            }
        }
        fetchAllProperties(getProvider().match(headers));
    }
    
    @Test
    public void duplicateHeadersNull() throws IOException {
        Map<String, String> headers = new HashMap<String, String>();
        for (int i = 0; i < 5; i++) {
            for (String header : getDataset().getHttpHeaders()) {
                headers.put(header, null);
            }
        }
        fetchAllProperties(getProvider().match(headers));
    }
    
    @Test
    public void emptyHeaders() throws IOException {
        fetchAllProperties(getProvider().match(new HashMap<String, String>()));
    }
    
    @Test
    public void emptyUserAgent() throws IOException {
        fetchAllProperties(getProvider().match(""));
    }
    
    @Test
    public void longUserAgent() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(getRandomUserAgent(10));
        }
        String userAgent = sb.toString();
        fetchAllProperties(getProvider().match(userAgent));
    }
    
    @Test
    public void nullHeaders() throws IOException {
        fetchAllProperties(getProvider().match((Map<String, String>) null));
    }
    
    /**
     * Validates that the detection system can handle null User-Agent inputs.
     * @throws IOException 
     */
    @Test
    public void nullUserAgent() throws IOException {
        fetchAllProperties(getProvider().match((String) null));
    }
    
    /**
     * Validates that all profiles can be retrieved from the profile Id with
     * the findProfile method of the data set.
     * @throws IOException 
     */
    @Test 
    public void fetchProfiles() throws IOException {
        int lastProfileId = getHighestProfileId();
        for (int i = 0; i <= lastProfileId; i++) {
            Profile profile = getProvider().dataSet.findProfile(i);
            if (profile != null) {
                assertTrue(profile.profileId == i);
                fetchAllProperties(profile);
            }
        }
    }
    
    /**
     * This method primarily tests the matchForDeviceId method of Provider class 
     * and getDeviceIdAsByteArray method of Match class.
     * @throws IOException propagated by the getDataset and getProvider methods 
     * and usually indicate there was a problem accessing the 51Degrees data 
     * file.
     */
    @Test
    public void deviceId() throws IOException {
        Random r = new Random();
        // Get deviceId as a list of profile IDs
        ArrayList<Integer> deviceId = new ArrayList<Integer>();
        byte[] deviceIdByteArray;
        String deviceIdString;
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < getDataset().getComponents().size(); i++) {
            // Get component.
            Component component = getDataset().getComponents().get(i);
            // Generate random number in range from 0 to max component index in 
            // the list of profiles associated with the current component.
            int randomProfile = r.nextInt(component.getProfiles().length);
            // Get the profile with Id generated and retrieve its Id.
            // Seems pointless but could potentially prevent errors by actually 
            // verifying that the profile with provided Id exists.
            int profileId = component.getProfiles()[randomProfile].profileId;
            deviceId.add(profileId);
            sb.append(profileId);
            if (i < getDataset().getComponents().size() - 1) {
                sb.append("-");
            }
        }
        // String version of Id.
        deviceIdString = sb.toString();
        // Get device Id in the form of a byte array.
        // Allocate byte array to store Id in the byte form.
        deviceIdByteArray = new byte[(deviceId.size() * Integer.SIZE / 8)];
        ByteBuffer bb = ByteBuffer.wrap(deviceIdByteArray);
        for (int profileId : deviceId) {
            bb.putInt(profileId);
        }
        
        // Test the respective match methods.
        Match matchDeviceId = getProvider().matchForDeviceId(deviceId);
        Match matchDeviceIdString = 
                getProvider().matchForDeviceId(deviceIdString);
        Match matchDeviceIdArray = 
                getProvider().matchForDeviceId(deviceIdByteArray);
        // Now assert the results are valid.
        assertTrue(matchDeviceId.getDeviceId().equals(deviceIdString));
        assertTrue(matchDeviceIdString.getDeviceId().equals(deviceIdString));
        assertTrue(matchDeviceIdArray.getDeviceId().equals(deviceIdString));
        assertTrue(Arrays.equals(   matchDeviceId.getDeviceIdAsByteArray(), 
                                    deviceIdByteArray));
        assertTrue(Arrays.equals(   matchDeviceIdString.getDeviceIdAsByteArray(), 
                                    deviceIdByteArray));
        assertTrue(Arrays.equals(   matchDeviceIdArray.getDeviceIdAsByteArray(),
                                    deviceIdByteArray));
    }
    
    private int getHighestProfileId() {
        int lastProfileId = 0;
        for (Profile profile : getProvider().dataSet.profiles) {
            if (profile.profileId > lastProfileId) {
                lastProfileId = profile.profileId;
            }
        }
        return lastProfileId;
    }
    
    private void fetchAllProperties(Profile profile) throws IOException {
        long checksum = 0;
        for (Property property : profile.getProperties()) {
            String propName = property.getName();
            Values values = profile.getValues(property);
            logger.debug("Property {}: {}", propName, values);
            if (values == null) {
                fail("Null value found for property " + propName );
            } else {
                checksum += values.hashCode();
            }                    
        }      
        logger.debug("Checksum: {}", checksum);
    }
    
    private void fetchAllProperties(Match match) throws IOException {
        long checksum = 0;
        for (Property property : match.getDataSet().getProperties()) {
            String propName = property.getName();
            Values values = match.getValues(property);
            logger.debug("Property {}: {}", propName, values);
            if (values == null) {
                fail("Null value found for property " + propName );
            } else {
                checksum += values.hashCode();
            }
        }
        logger.debug("Checksum: {}", checksum);
    }
}
