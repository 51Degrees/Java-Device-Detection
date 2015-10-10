/*
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited.
 * Copyright © 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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

import fiftyone.mobile.detection.TrieProvider;
import fiftyone.mobile.detection.test.DetectionTestSupport;
import fiftyone.mobile.detection.test.TestType;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.HashMap;
import java.util.Map;

/**
 * Super class containing Api Trie tests. Subclassed to test Tries data sets at
 * various revisions.
 * <p>
 * Beware: The class level {@link Category} annotation does not seem to work
 * without one of the test methods being annotated
 */
@Category(TestType.TypeApi.class)
public abstract class ApiTrieBase extends DetectionTestSupport {

    public abstract TrieProvider getProvider();

    // NB Do not remove the @Category annotation it seems to be needed to trigger the
    // correct operation of the class level annotation
    @Test
    @Category(TestType.TypeApi.class)
    public void allHeaders() {
        Map<String, String> headers = new HashMap<String, String>();
        for (String header : getProvider().getHttpHeaders()) {
            headers.put(header, fiftyone.mobile.detection.test.common.UserAgentGenerator.getRandomUserAgent(0));
        }
        fetchAllProperties(getProvider().getDeviceIndexes(headers));
    }
    
    @Test
    public void allHeadersNull() {
        Map<String, String> headers = new HashMap<String, String>();
        for (String header : getProvider().getHttpHeaders()) {
            headers.put(header, null);
        }
        fetchAllProperties(getProvider().getDeviceIndexes(headers));
    }
    
    @Test
    public void duplicateHeaders() {
        Map<String, String> headers = new HashMap<String, String>();
        for (int i = 0; i < 5; i++) {
            for (String header : getProvider().getHttpHeaders()) {
                headers.put(header, fiftyone.mobile.detection.test.common.UserAgentGenerator.getRandomUserAgent(0));
            }
        }
        fetchAllProperties(getProvider().getDeviceIndexes(headers));
    }
    
    @Test
    public void duplicateHeadersNull() {
        Map<String, String> headers = new HashMap<String, String>();
        for (int i = 0; i < 5; i++) {
            for (String header : getProvider().getHttpHeaders()) {
                headers.put(header, null);
            }
        }
        fetchAllProperties(getProvider().getDeviceIndexes(headers));
    }
    
    @Test
    public void emptyHeaders() {
        fetchAllProperties(getProvider().getDeviceIndexes(new HashMap<String, String>()));
    }
    
    @Test
    public void emptyUserAgent() throws Exception {
        fetchAllProperties(getProvider().getDeviceIndex(""));
    }
    
    @Test
    public void longUserAgent() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(fiftyone.mobile.detection.test.common.UserAgentGenerator.getRandomUserAgent(10));
        }
        fetchAllProperties(getProvider().getDeviceIndex(sb.toString()));
    }
    
    @Test
    public void nullHeaders() {
        fetchAllProperties(getProvider().getDeviceIndexes(null));
    }
    
    @Test
    public void nullUserAgent() throws Exception {
        fetchAllProperties(getProvider().getDeviceIndex(null));
    }
    
    /* Support methods */
    
    private void fetchAllProperties(Map<String, Integer> deviceIndexes) {
        int checksum = 0;
        for (String propertyName : getProvider().propertyNames()) {
            String value = getProvider().getPropertyValue(deviceIndexes, propertyName);
            logger.debug("{}: {}", propertyName, value);
            if (value != null) {
                checksum += value.hashCode();
            }
        }
        logger.debug("Checksum: {}", checksum);
    }
    
    private void fetchAllProperties(int deviceIndex) {
        int checksum = 0;
        for (String propertyName : getProvider().propertyNames()) {
            String value = getProvider().getPropertyValue(deviceIndex, propertyName);
            logger.debug("{}: {}", propertyName, value);
            if (value != null) {
                checksum += value.hashCode();
            }
        }
        logger.debug("Checksum: {}", checksum);
    }
}
