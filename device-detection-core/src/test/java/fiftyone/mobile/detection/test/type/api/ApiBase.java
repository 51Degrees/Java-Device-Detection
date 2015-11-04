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
import fiftyone.mobile.detection.Utilities;
import fiftyone.mobile.detection.entities.Component;
import fiftyone.mobile.detection.entities.Values;
import fiftyone.mobile.detection.test.TestType;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static fiftyone.mobile.detection.test.common.UserAgentGenerator.getRandomUserAgent;
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
    public void LongUserAgent() throws IOException {
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
    
    @Test
    public void nullUserAgent() throws IOException {
        fetchAllProperties(getProvider().match((String) null));
    }
    
    @Test
    public void deviceId() throws IOException {
        Random r = new Random();
        StringBuilder sbId = new StringBuilder();
        StringBuilder sbBytes = new StringBuilder();
        
        ArrayList<Integer> deviceId = new ArrayList<Integer>();
        String deviceIdString;
        byte[] deviceIdArray;
        
        // Get one random profile for each component.
        for (int i = 0; i < getDataset().getComponents().size(); i++) {
            // First get component.
            Component component = getDataset().getComponents().get(i);
            // Generate a random number in range of zero to last profile of current component.
            int profile = r.nextInt(component.getProfiles().length);   
            int id = component.getProfiles()[profile].profileId;
            deviceId.add(id);
            sbId.append(id);
            sbBytes.append(id);
            if (i < getDataset().getComponents().size() - 1) {
                sbId.append("-");
            }
        }
        deviceIdString = sbId.toString();
        deviceIdArray = sbBytes.toString().getBytes();
        
        for (byte b : deviceIdArray) {
            System.out.println("->"+Integer.toBinaryString(b & 255 | 256).substring(1));
        }
        
        logger.debug("Device Id string is:           "+deviceIdString);
        //byte[] deviceIdArray = deviceIdString.getBytes();
        
        logger.debug("length of byte array: "+deviceIdArray.length);
        
        Match matchDeviceId = getProvider().matchForDeviceId(deviceId);
        Match matchDeviceIdString = getProvider().matchForDeviceId(deviceIdString);
        Match matchDeviceIdArray = getProvider().matchForDeviceId(deviceIdArray);
        logger.debug("Match with list of integers: \t"+matchDeviceId.getDeviceId());
        logger.debug("Match with string ID:        \t"+matchDeviceIdString.getDeviceId());
        logger.debug("Match with byte array:       \t"+matchDeviceIdArray.getDeviceId());
        
        /*
        assertTrue(matchDeviceId.getDeviceId().equals(deviceIdString));
        assertTrue(matchDeviceIdString.getDeviceId().equals(deviceIdString));
        assertTrue(matchDeviceIdArray.getDeviceId().equals(deviceIdString));
        /*
        assertTrue(Arrays.equals(matchDeviceId.getDeviceId().getBytes(), deviceIdArray));
        assertTrue(Arrays.equals(matchDeviceIdString.getDeviceId().getBytes(), deviceIdArray));
        assertTrue(Arrays.equals(matchDeviceIdArray.getDeviceId().getBytes(), deviceIdArray));
                */
    }
    
    private void fetchAllProperties(Match match) throws IOException {
        long checksum = 0;
        for (Property property : match.getDataSet().getProperties()) {
            String propName = property.getName();
            Values values = match.getValues(property);
            logger.debug("Property {}: {}", propName, values);
            if (match.getValues(property) == null) {
                fail("Null value found for property " + propName );
            } else {
                checksum += values.hashCode();
            }
        }
        logger.debug("Checksum: {}", checksum);
    }
}
