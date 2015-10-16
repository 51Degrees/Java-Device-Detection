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

package fiftyone.mobile.detection.test.type.httpheader;

import fiftyone.mobile.detection.DetectionTestSupport;
import fiftyone.mobile.detection.test.common.Results;
import fiftyone.mobile.detection.test.common.UserAgentGenerator;
import fiftyone.mobile.detection.TrieProvider;
import fiftyone.mobile.detection.test.TestType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Pattern;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.experimental.categories.Category;

@Category(TestType.TypeHttpHeader.class)
public class TrieBase extends DetectionTestSupport {

    protected String dataFile;
    
    protected TrieProvider provider;
    
    public void setUp() {
        System.out.printf("Setup test with file '%s'\r\n", dataFile);
    }
    
    public TrieBase(String dataFile) {
        this.dataFile = dataFile;
    }
    
    protected Results process(String userAgentPattern, String devicePattern, TrieValidation state) 
                                                                            throws IOException, Exception
    {
        Results results = new Results();
        Random random = new Random();
        String httpHeaders[] = new String[provider.getHttpHeaders().size() - 1];
        
        // Copy the HTTP headers from the data set to the local list ignoring
        // any which are the User-Agent header.
        int index = 0, dataSetIndex = 0;
        while (index < httpHeaders.length) {
            if (provider.getHttpHeaders().get(dataSetIndex).equals("User-Agent") == false) {
                httpHeaders[index] = provider.getHttpHeaders().get(dataSetIndex);
                index++;
            }
            dataSetIndex++;
        }
        
        // Loop through setting 2 user agent headers.
        Iterator<String> userAgentIterator = UserAgentGenerator.getUserAgentsIterable(userAgentPattern).iterator();
        Iterator<String> deviceIterator = UserAgentGenerator.getUserAgentsIterable(devicePattern).iterator();
        
        while(userAgentIterator.hasNext()&& deviceIterator.hasNext())
        {
            Map<String, String> headers = new HashMap<String, String>();
            headers.put(httpHeaders[random.nextInt(httpHeaders.length)], deviceIterator.next());
            headers.put("User-Agent", userAgentIterator.next());
            Map<String, Integer> indexes = provider.getDeviceIndexes(headers);
            assertTrue("No indexes were found", indexes.size() > 0);
            validate(indexes, state);
        }
        
        return results;
    }
    
    private static void validate(Map<String, Integer> indexes, TrieValidation validation) {
        for (Entry<String, Pattern> test : validation.entrySet()) {
            String value = validation.provider.getPropertyValue(indexes, test.getKey());
            assertTrue(String.format(
                    "HttpHeader test failed for Property '%s' and test '%s' with result '%s'",
                    test.getKey(),
                    test.getValue(),
                    value),
                test.getValue().matcher(value).matches());
        }
    }
    
    /**
     * Ensures the data set is disposed of correctly.
     * @throws Throwable 
     */
    @Override
    protected void finalize() throws Throwable {
        disposing(false);
        super.finalize();
    }
    
    /**
     * Ensures resources used by the data set are closed and memory released.
     * @param disposing 
     */
    protected void disposing(boolean disposing) {
        if (provider != null) {
            provider.close();
            provider = null;
        }
    }
    
    /**
     * Ensures the data set is disposed of correctly at the end of the test.
     * @throws Exception 
     */
    @After
    public void tearDown() throws Exception {
        disposing(true);
    }
}
