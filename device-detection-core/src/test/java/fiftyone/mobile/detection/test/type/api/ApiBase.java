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

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.Provider;
import fiftyone.mobile.detection.entities.Property;
import fiftyone.mobile.detection.test.DetectionTestSupport;
import fiftyone.mobile.detection.test.TestType;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static fiftyone.mobile.detection.test.common.UserAgentGenerator.getRandomUserAgent;

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
    
    private void fetchAllProperties(Match match) throws IOException {
        long checksum = 0;
        for (Property property : match.dataSet.getProperties()) {
            String propName = property.getName();
            logger.debug("Property {}: {}", propName, match.getValues(property));
            if (match.getValues(property) != null) {
                checksum += match.getValues(propName).hashCode();
            }
        }
        logger.debug("Checksum: {}", checksum);
    }
}
